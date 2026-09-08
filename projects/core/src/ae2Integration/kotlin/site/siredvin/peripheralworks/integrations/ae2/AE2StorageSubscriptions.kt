package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.networking.GridHelper
import appeng.api.networking.IGrid
import appeng.api.networking.IGridConnection
import appeng.api.networking.IGridNode
import appeng.api.networking.IGridNodeListener
import appeng.api.networking.IManagedGridNode
import appeng.api.networking.IStackWatcher
import appeng.api.networking.storage.IStorageWatcherNode
import appeng.api.stacks.AEFluidKey
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.AEKey
import appeng.api.stacks.KeyCounter
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import site.siredvin.broccolium.modules.platform.PlatformRegistries
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.integration.AE2Configuration
import site.siredvin.tweakium.modules.peripheral.api.IDataStorage
import site.siredvin.tweakium.modules.peripheral.api.IExpandedPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation
import site.siredvin.tweakium.modules.peripheral.representation.RepresentationMode
import site.siredvin.tweakium.modules.plugins.PeripheralPluginUtils
import java.io.DataOutputStream
import java.io.OutputStream
import java.util.TreeMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.function.Predicate

private const val SUBSCRIPTIONS_TAG = "ae2StorageSubscriptions"
private const val DEFINITIONS_TAG = "subscriptions"
private const val FILTER_TAG = "filter"
private const val VALUE_TYPE_TAG = "type"
private const val VALUE_TAG = "value"
private const val ENTRIES_TAG = "entries"
private const val KEY_TAG = "key"
private const val MAX_FILTER_DEPTH = 16
private const val MAX_FILTER_BYTES = 65536
private const val MAX_NBT_STRING_BYTES = 65535

private const val STRING_VALUE: Byte = 1
private const val NUMBER_VALUE: Byte = 2
private const val BOOLEAN_VALUE: Byte = 3
private const val MAP_VALUE: Byte = 4

internal enum class AE2StorageSubscriptionType(val serializedName: String) {
    ITEM("item"),
    FLUID("fluid"),
    ;

    companion object {
        fun parse(value: String): AE2StorageSubscriptionType = entries.firstOrNull { it.serializedName == value }
            ?: throw LuaException("Subscription type must be 'item' or 'fluid'")
    }
}

internal data class AE2StorageSubscriptionDefinition(
    val name: String,
    val type: AE2StorageSubscriptionType,
    val filter: Any?,
) {
    fun toLua(): Map<String, Any> = mutableMapOf<String, Any>(
        "name" to name,
        "type" to type.serializedName,
    ).apply {
        if (filter != null) put("filter", filter)
    }
}

private data class RuntimeSubscription(
    val definition: AE2StorageSubscriptionDefinition,
    val itemPredicate: Predicate<net.minecraft.world.item.ItemStack>? = null,
    val fluidName: ResourceLocation? = null,
) {
    fun matches(key: AEKey): Boolean = when {
        definition.type == AE2StorageSubscriptionType.ITEM && key is AEItemKey -> itemPredicate!!.test(key.toStack(1))
        definition.type == AE2StorageSubscriptionType.FLUID && key is AEFluidKey -> fluidName == null || PlatformRegistries.FLUIDS.getKey(key.fluid) == fluidName
        else -> false
    }
}

class AE2StorageSubscriptionTracker(
    private val maxSubscriptions: Int = AE2Configuration.maxSubscriptions,
    private val maxItemFilterSize: Int = AE2Configuration.maxItemFilterSize,
) {
    private val subscriptions = TreeMap<String, RuntimeSubscription>()
    private val amounts = mutableMapOf<AEKey, Long>()

    // Attach/detach may run on computer threads; dispatch uses a stable snapshot.
    private val eventSinks = CopyOnWriteArraySet<(String, Map<String, Any>, Long, Long) -> Unit>()
    private var baselined = false

    var onDefinitionsChanged: (() -> Unit)? = null
    var onActivityChanged: (() -> Unit)? = null

    val shouldObserve: Boolean
        get() = subscriptions.isNotEmpty() && eventSinks.isNotEmpty()

    fun subscribe(name: String, typeName: String, filter: Any?) {
        validateName(name)
        if (name !in subscriptions && subscriptions.size >= maxSubscriptions) {
            throw LuaException("Peripheral cannot have more than $maxSubscriptions AE2 storage subscriptions")
        }
        val type = AE2StorageSubscriptionType.parse(typeName)
        val normalizedFilter = normalizeFilter(type, filter)
        val definition = AE2StorageSubscriptionDefinition(name, type, normalizedFilter)
        val runtime = compile(definition)
        val wasObserving = shouldObserve
        val previous = subscriptions.put(name, runtime)
        try {
            onDefinitionsChanged?.invoke()
        } catch (error: Exception) {
            if (previous == null) subscriptions.remove(name) else subscriptions[name] = previous
            throw error
        }
        if (wasObserving != shouldObserve) onActivityChanged?.invoke()
    }

    fun unsubscribe(name: String): Boolean {
        val wasObserving = shouldObserve
        val removed = subscriptions.remove(name) != null
        if (removed) {
            onDefinitionsChanged?.invoke()
            if (wasObserving != shouldObserve) onActivityChanged?.invoke()
        }
        return removed
    }

    fun getSubscriptions(): List<Map<String, Any>> = subscriptions.values.map { it.definition.toLua() }

    fun addEventSink(sink: (String, Map<String, Any>, Long, Long) -> Unit) {
        // Do not read the server-owned subscription map from an attachment thread.
        if (eventSinks.add(sink)) onActivityChanged?.invoke()
    }

    fun removeEventSink(sink: (String, Map<String, Any>, Long, Long) -> Unit) {
        if (eventSinks.remove(sink)) onActivityChanged?.invoke()
    }

    fun baseline(counter: KeyCounter) {
        amounts.clear()
        counter.forEach { entry ->
            if (isSupported(entry.key) && entry.longValue > 0) amounts[entry.key] = entry.longValue
        }
        baselined = true
    }

    fun clearBaseline() {
        baselined = false
        amounts.clear()
    }

    fun onStackChange(key: AEKey, currentAmount: Long) {
        if (!isSupported(key)) return
        val previousAmount = amounts[key] ?: 0
        if (currentAmount > 0) amounts[key] = currentAmount else amounts.remove(key)
        if (!baselined) return

        val previousPublic = AE2Helper.publicAmount(key, previousAmount)
        val currentPublic = AE2Helper.publicAmount(key, currentAmount)
        if (previousPublic == currentPublic) return
        val resource = resourceToLua(key)
        subscriptions.values.forEach { subscription ->
            if (subscription.matches(key)) {
                eventSinks.forEach { it(subscription.definition.name, resource, previousPublic, currentPublic) }
            }
        }
    }

    fun save(): CompoundTag = CompoundTag().apply {
        val definitions = ListTag()
        subscriptions.values.forEach { runtime ->
            definitions.add(
                CompoundTag().apply {
                    putString("name", runtime.definition.name)
                    putString("subscriptionType", runtime.definition.type.serializedName)
                    runtime.definition.filter?.let { put(FILTER_TAG, encodeValue(it, 0, intArrayOf(0), maxItemFilterSize)) }
                },
            )
        }
        put(DEFINITIONS_TAG, definitions)
    }

    fun load(tag: CompoundTag): Boolean {
        subscriptions.clear()
        var malformed = false
        val definitions = tag.getList(DEFINITIONS_TAG, Tag.TAG_COMPOUND.toInt())
        if (definitions.size > maxSubscriptions) malformed = true
        definitions.take(maxSubscriptions).forEach { raw ->
            try {
                val entry = raw as CompoundTag
                val name = entry.getString("name")
                validateName(name)
                val type = AE2StorageSubscriptionType.parse(entry.getString("subscriptionType"))
                val filter = if (entry.contains(FILTER_TAG, Tag.TAG_COMPOUND.toInt())) {
                    decodeValue(entry.getCompound(FILTER_TAG), 0, intArrayOf(0), maxItemFilterSize)
                } else {
                    null
                }
                val definition = AE2StorageSubscriptionDefinition(name, type, normalizeFilter(type, filter))
                subscriptions[name] = compile(definition)
            } catch (_: Exception) {
                malformed = true
            }
        }
        clearBaseline()
        onActivityChanged?.invoke()
        return malformed
    }

    private fun normalizeFilter(type: AE2StorageSubscriptionType, filter: Any?): Any? = when (type) {
        AE2StorageSubscriptionType.ITEM -> {
            if (filter != null && filter !is String && filter !is Map<*, *>) throw LuaException("Item query should be string or table")
            normalizeValue(filter, 0, intArrayOf(0), maxItemFilterSize).also {
                PeripheralPluginUtils.itemQueryToPredicate(it)
                if (it != null) {
                    val encoded = encodeValue(it, 0, intArrayOf(0), maxItemFilterSize)
                    DataOutputStream(OutputStream.nullOutputStream()).use { output ->
                        NbtIo.write(encoded, output)
                        if (output.size() > MAX_FILTER_BYTES) throw LuaException("Subscription filter exceeds $MAX_FILTER_BYTES encoded bytes")
                    }
                }
            }
        }
        AE2StorageSubscriptionType.FLUID -> {
            if (filter == null) {
                null
            } else {
                if (filter !is String) throw LuaException("Fluid filter must be a registry name")
                val id = ResourceLocation.tryParse(filter) ?: throw LuaException("Invalid fluid '$filter'")
                if (id !in PlatformRegistries.FLUIDS.keySet()) throw LuaException("Unknown fluid '$filter'")
                id.toString()
            }
        }
    }

    private fun compile(definition: AE2StorageSubscriptionDefinition): RuntimeSubscription = when (definition.type) {
        AE2StorageSubscriptionType.ITEM -> RuntimeSubscription(
            definition,
            itemPredicate = PeripheralPluginUtils.itemQueryToPredicate(definition.filter),
        )
        AE2StorageSubscriptionType.FLUID -> RuntimeSubscription(
            definition,
            fluidName = (definition.filter as? String)?.let(ResourceLocation::tryParse),
        )
    }

    private fun resourceToLua(key: AEKey): Map<String, Any> = when (key) {
        is AEItemKey -> LuaRepresentation.forItemStack(key.toStack(1), RepresentationMode.DETAILED).apply {
            remove("count")
            put("type", "item")
        }
        is AEFluidKey -> mapOf(
            "type" to "fluid",
            "name" to PlatformRegistries.FLUIDS.getKey(key.fluid).toString(),
        )
        else -> error("Unsupported AE2 key")
    }

    private fun isSupported(key: AEKey): Boolean = key is AEItemKey || key is AEFluidKey
}

internal class AE2StorageWatcherNode(
    private val tracker: AE2StorageSubscriptionTracker,
    private val resolveGrid: () -> IGrid?,
) : IStorageWatcherNode {
    private var watcher: IStackWatcher? = null
    private var observedGrid: IGrid? = null

    override fun updateWatcher(watcher: IStackWatcher) {
        this.watcher?.reset()
        this.watcher = watcher
        observedGrid = null
        tracker.clearBaseline()
        refresh()
    }

    override fun onStackChange(key: AEKey, amount: Long) {
        tracker.onStackChange(key, amount)
    }

    fun refresh(force: Boolean = false) {
        val watcher = watcher ?: return
        val grid = if (tracker.shouldObserve) resolveGrid() else null
        if (grid == null) {
            stop()
            return
        }
        if (!force && observedGrid === grid) return
        watcher.reset()
        tracker.baseline(grid.storageService.cachedInventory)
        observedGrid = grid
        watcher.setWatchAll(true)
    }

    fun stop() {
        watcher?.reset()
        observedGrid = null
        tracker.clearBaseline()
    }
}

class AE2StorageSubscriptionPlugin(
    val tracker: AE2StorageSubscriptionTracker,
) : IPeripheralPlugin {
    override var connectedPeripheral: IExpandedPeripheral? = null

    private val eventSink: (String, Map<String, Any>, Long, Long) -> Unit = { name, resource, previous, current ->
        connectedPeripheral?.queueEvent("ae2_storage_change", name, resource, previous, current)
    }

    override fun onFirstAttach() {
        tracker.addEventSink(eventSink)
    }

    override fun onLastDetach() {
        tracker.removeEventSink(eventSink)
    }

    @LuaFunction(mainThread = true)
    fun subscribe(name: String, type: String, filter: Any?) {
        tracker.subscribe(name, type, filter)
    }

    @LuaFunction(mainThread = true)
    fun unsubscribe(name: String): Boolean = tracker.unsubscribe(name)

    @LuaFunction(mainThread = true)
    fun getSubscriptions(): List<Map<String, Any>> = tracker.getSubscriptions()
}

internal fun CompoundTag.putAE2StorageSubscriptions(tracker: AE2StorageSubscriptionTracker) {
    put(SUBSCRIPTIONS_TAG, tracker.save())
}

internal fun CompoundTag.loadAE2StorageSubscriptions(tracker: AE2StorageSubscriptionTracker): Boolean = if (contains(SUBSCRIPTIONS_TAG, Tag.TAG_COMPOUND.toInt())) {
    tracker.load(getCompound(SUBSCRIPTIONS_TAG))
} else {
    tracker.load(CompoundTag())
}

internal fun IDataStorage.putAE2StorageSubscriptions(tracker: AE2StorageSubscriptionTracker) {
    putCompound(SUBSCRIPTIONS_TAG, tracker.save())
}

internal fun IDataStorage.loadAE2StorageSubscriptions(tracker: AE2StorageSubscriptionTracker): Boolean = if (has(SUBSCRIPTIONS_TAG)) {
    tracker.load(getCompound(SUBSCRIPTIONS_TAG))
} else {
    tracker.load(CompoundTag())
}

object AE2StorageSubscriptionPluginProvider : PeripheralPluginProvider {
    override val pluginType = "ae2_storage_subscriptions"

    override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
        if (!AE2Configuration.enableMEInterface) return null
        val entity = level.getBlockEntity(pos) as? MENetworkPeripheralBlockEntity ?: return null
        return AE2StorageSubscriptionPlugin(entity.subscriptionTracker)
    }
}

internal class AE2WirelessStorageObserver(private val tracker: AE2StorageSubscriptionTracker) {
    private var managedNode: IManagedGridNode? = null
    private var connection: IGridConnection? = null
    private var targetNode: IGridNode? = null
    private var watcherService: AE2StorageWatcherNode? = null

    fun update(level: Level, pos: BlockPos, target: IGridNode) {
        if (!tracker.shouldObserve) {
            destroy()
            return
        }
        val currentNode = managedNode?.node
        if (targetNode === target && currentNode?.grid === target.grid) {
            watcherService?.refresh()
            return
        }

        destroy()
        try {
            lateinit var observerNode: IManagedGridNode
            val service = AE2StorageWatcherNode(tracker) {
                observerNode.grid?.takeIf { targetNode != null && targetNode?.grid === it }
            }
            observerNode = GridHelper.createManagedNode(
                this,
                IGridNodeListener<AE2WirelessStorageObserver> { _, _ -> },
            ).setInWorldNode(false)
                .setIdlePowerUsage(0.0)
                .setTagName("upw_storage_subscription")
                .addService(IStorageWatcherNode::class.java, service)
            managedNode = observerNode
            watcherService = service
            observerNode.create(level, pos)
            connection = GridHelper.createConnection(observerNode.node, target)
            targetNode = target
            service.refresh(force = true)
        } catch (_: Exception) {
            destroy()
        }
    }

    fun destroy() {
        val oldConnection = connection
        val oldNode = managedNode
        connection = null
        managedNode = null
        targetNode = null
        watcherService?.stop()
        watcherService = null
        oldConnection?.destroy()
        oldNode?.destroy()
        tracker.clearBaseline()
    }
}

private fun validateName(name: String) {
    if (name.isBlank()) throw LuaException("Subscription name must not be empty")
    checkStringBytes(name, MAX_NBT_STRING_BYTES)
}

private fun checkStringBytes(value: String, limit: Int) {
    if (value.length > limit) throw LuaException("Subscription string exceeds $limit NBT bytes")
    var bytes = 0
    for (char in value) {
        // NBT uses modified UTF-8: NUL takes two bytes and surrogates take three each.
        bytes += when (char.code) {
            in 1..127 -> 1
            in 0..2047 -> 2
            else -> 3
        }
        if (bytes > limit) throw LuaException("Subscription string exceeds $limit NBT bytes")
    }
}

private fun normalizeValue(value: Any?, depth: Int, count: IntArray, maxValues: Int): Any? {
    if (value == null) return null
    if (depth > MAX_FILTER_DEPTH || ++count[0] > maxValues) throw LuaException("Subscription item filter exceeds the configured size limit of $maxValues values")
    return when (value) {
        is String -> value.also { checkStringBytes(it, MAX_NBT_STRING_BYTES) }
        is Boolean -> value
        is Number -> value.toDouble().also {
            if (!it.isFinite()) throw LuaException("Subscription filter contains an invalid number")
        }
        is Map<*, *> -> LinkedHashMap<Any, Any>().apply {
            value.forEach { (key, nested) ->
                val normalizedKey = when (key) {
                    is String -> key
                    is Number -> key.toDouble().also {
                        if (!it.isFinite()) throw LuaException("Subscription filter contains an invalid key")
                    }
                    else -> throw LuaException("Subscription filter table keys must be strings or numbers")
                }
                normalizeValue(normalizedKey, depth + 1, count, maxValues)
                put(normalizedKey, normalizeValue(nested, depth + 1, count, maxValues) ?: throw LuaException("Subscription filter table values must not be nil"))
            }
        }
        else -> throw LuaException("Subscription filter contains an unsupported value")
    }
}

private fun encodeValue(value: Any, depth: Int, count: IntArray, maxValues: Int): CompoundTag {
    if (depth > MAX_FILTER_DEPTH || ++count[0] > maxValues) throw IllegalArgumentException("Subscription filter is too complex")
    return CompoundTag().apply {
        when (value) {
            is String -> {
                putByte(VALUE_TYPE_TAG, STRING_VALUE)
                putString(VALUE_TAG, value)
            }
            is Number -> {
                putByte(VALUE_TYPE_TAG, NUMBER_VALUE)
                putDouble(VALUE_TAG, value.toDouble())
            }
            is Boolean -> {
                putByte(VALUE_TYPE_TAG, BOOLEAN_VALUE)
                putBoolean(VALUE_TAG, value)
            }
            is Map<*, *> -> {
                putByte(VALUE_TYPE_TAG, MAP_VALUE)
                put(
                    ENTRIES_TAG,
                    ListTag().apply {
                        value.forEach { (key, nested) ->
                            add(
                                CompoundTag().apply {
                                    put(KEY_TAG, encodeValue(key ?: throw IllegalArgumentException("Missing filter key"), depth + 1, count, maxValues))
                                    put(VALUE_TAG, encodeValue(nested ?: throw IllegalArgumentException("Missing filter value"), depth + 1, count, maxValues))
                                },
                            )
                        }
                    },
                )
            }
            else -> throw IllegalArgumentException("Unsupported filter value")
        }
    }
}

private fun decodeValue(tag: CompoundTag, depth: Int, count: IntArray, maxValues: Int): Any {
    if (depth > MAX_FILTER_DEPTH || ++count[0] > maxValues) throw IllegalArgumentException("Subscription filter is too complex")
    return when (tag.getByte(VALUE_TYPE_TAG)) {
        STRING_VALUE -> tag.getString(VALUE_TAG)
        NUMBER_VALUE -> tag.getDouble(VALUE_TAG).also { if (!it.isFinite()) throw IllegalArgumentException("Invalid filter number") }
        BOOLEAN_VALUE -> tag.getBoolean(VALUE_TAG)
        MAP_VALUE -> LinkedHashMap<Any, Any>().apply {
            tag.getList(ENTRIES_TAG, Tag.TAG_COMPOUND.toInt()).forEach { raw ->
                val entry = raw as CompoundTag
                val key = decodeValue(entry.getCompound(KEY_TAG), depth + 1, count, maxValues)
                if (key !is String && key !is Number) throw IllegalArgumentException("Invalid filter key")
                put(key, decodeValue(entry.getCompound(VALUE_TAG), depth + 1, count, maxValues))
            }
        }
        else -> throw IllegalArgumentException("Unknown filter value type")
    }
}
