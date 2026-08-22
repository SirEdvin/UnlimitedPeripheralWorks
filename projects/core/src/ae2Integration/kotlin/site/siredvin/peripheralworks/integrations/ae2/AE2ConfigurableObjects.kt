package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.config.*
import appeng.api.crafting.PatternDetailsHelper
import appeng.api.inventories.InternalInventory
import appeng.api.stacks.AEFluidKey
import appeng.api.upgrades.IUpgradeInventory
import appeng.block.crafting.PatternProviderBlock
import appeng.block.crafting.PushDirection
import appeng.blockentity.crafting.PatternProviderBlockEntity
import appeng.blockentity.misc.InterfaceBlockEntity
import appeng.blockentity.networking.CableBusBlockEntity
import appeng.core.definitions.AEItems
import appeng.helpers.IPriorityHost
import appeng.helpers.InterfaceLogicHost
import appeng.helpers.externalstorage.GenericStackInv
import appeng.helpers.patternprovider.PatternProviderLogicHost
import appeng.parts.automation.*
import appeng.parts.crafting.PatternProviderPart
import appeng.parts.misc.InterfacePart
import appeng.parts.storagebus.StorageBusPart
import appeng.util.ConfigInventory
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.broccolium.modules.storage.base.api.SlottedAgnosticSink
import site.siredvin.broccolium.modules.storage.base.api.SlottedAgnosticStorage
import site.siredvin.broccolium.modules.storage.item.AgnosticItemSinkLookup
import site.siredvin.broccolium.modules.storage.item.AgnosticItemStorageLookup
import site.siredvin.broccolium.modules.storage.item.ContainerWrapper
import site.siredvin.broccolium.modules.storage.item.ItemStorageUtils
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.tweakium.modules.peripheral.api.IExpandedPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import site.siredvin.tweakium.modules.peripheral.api.ISidedPeripheral
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation
import site.siredvin.tweakium.modules.peripheral.util.assertBetween
import java.util.*
import kotlin.math.min

private fun itemDetails(stack: ItemStack): Map<String, Any> = LuaRepresentation.forItemStack(stack)

private fun parseDirection(value: String): Direction = Direction.byName(value)
    ?: throw LuaException("Direction must be north, south, east, west, up, or down")

private fun parseLimit(limit: Optional<Int>): Int = limit.orElse(Int.MAX_VALUE).also {
    if (it < 0) throw LuaException("Limit must be non-negative")
}

private fun invalidEnum(label: String, value: String, values: Iterable<String>): LuaException = LuaException("Invalid $label '$value'; expected one of: ${values.joinToString(", ")}")

private fun requireFuzzyCard(upgrades: IUpgradeInventory) {
    if (upgrades.getInstalledUpgrades(AEItems.FUZZY_CARD) == 0) throw LuaException("A Fuzzy Card is required")
}

private fun requireAttached(access: IComputerAccess, attached: (() -> Boolean)?) {
    if (attached != null && !attached()) throw LuaException("The originating computer is no longer attached")
}

private fun peripheral(access: IComputerAccess, name: String, source: Boolean): IPeripheral = access.getAvailablePeripheral(name)
    ?: throw LuaException("${if (source) "Source" else "Target"} '$name' does not exist")

private fun pullItem(
    level: Level,
    access: IComputerAccess,
    inventory: InternalInventory,
    fromName: String,
    fromSlot: Int,
    limit: Optional<Int>,
    toSlot: Optional<Int>,
    predicate: (ItemStack) -> Boolean,
): Int {
    val location = peripheral(access, fromName, true)
    val direction = (location as? ISidedPeripheral)?.side
    val source = AgnosticItemStorageLookup.extractFromUnknown(level, location.target, direction)
        ?: throw LuaException("Source '$fromName' is not an inventory")
    if (source !is SlottedAgnosticStorage<ItemStack, Int>) throw LuaException("Source '$fromName' is not slotted storage")
    assertBetween(fromSlot, 1, source.size, "fromSlot")
    if (toSlot.isPresent) assertBetween(toSlot.get(), 1, inventory.size(), "toSlot")
    val actualLimit = parseLimit(limit)
    if (actualLimit == 0 || !predicate(source.get(fromSlot - 1))) return 0
    val moved = ContainerWrapper(inventory.toContainer()).moveFrom(
        source,
        actualLimit,
        toSlot.orElse(0) - 1,
        fromSlot - 1,
        predicate,
    )
    if (moved > 0) for (slot in 0 until inventory.size()) inventory.sendChangeNotification(slot)
    return moved
}

private fun pushItem(
    level: Level,
    access: IComputerAccess,
    inventory: InternalInventory,
    toName: String,
    fromSlot: Int,
    limit: Optional<Int>,
    toSlot: Optional<Int>,
): Pair<Int, ItemStack> {
    assertBetween(fromSlot, 1, inventory.size(), "fromSlot")
    val location = peripheral(access, toName, false)
    val direction = (location as? ISidedPeripheral)?.side
    val target = AgnosticItemSinkLookup.extractFromUnknown(level, location.target, direction)
        ?: throw LuaException("Target '$toName' is not an inventory")
    if (toSlot.isPresent) {
        if (target !is SlottedAgnosticSink<ItemStack, Int>) throw LuaException("Target '$toName' is not slotted storage")
        assertBetween(toSlot.get(), 1, target.size, "toSlot")
    }
    val actualLimit = parseLimit(limit)
    if (actualLimit == 0) return 0 to ItemStack.EMPTY
    val stack = inventory.getStackInSlot(fromSlot - 1).copy()
    val moved = ContainerWrapper(inventory.toContainer()).moveTo(
        target,
        actualLimit,
        fromSlot - 1,
        toSlot.orElse(0) - 1,
        ItemStorageUtils.ALWAYS,
    )
    if (moved > 0) inventory.sendChangeNotification(fromSlot - 1)
    return moved to stack
}

private fun clearInactiveFilters(upgrades: IUpgradeInventory, config: ConfigInventory) {
    val active = min(18 + upgrades.getInstalledUpgrades(AEItems.CAPACITY_CARD) * 9, config.size())
    for (slot in active until config.size()) config.setStack(slot, null)
}

internal abstract class DeviceObject<T : Any>(
    private val deviceType: String,
    protected val level: Level,
    private val resolveDevice: () -> T,
) {
    protected fun device(): T = resolveDevice()

    protected open fun configuration(): Map<String, Any> = emptyMap()

    @LuaFunction(mainThread = true)
    fun getDeviceType(): String {
        device()
        return deviceType
    }

    @LuaFunction(mainThread = true)
    fun getConfiguration(): Map<String, Any> {
        device()
        return configuration()
    }
}

internal abstract class UpgradeableDeviceObject<T : Any>(
    deviceType: String,
    level: Level,
    resolveDevice: () -> T,
    private val upgrades: (T) -> IUpgradeInventory,
    private val config: ((T) -> ConfigInventory)? = null,
) : DeviceObject<T>(deviceType, level, resolveDevice) {
    protected fun upgradeInventory(): IUpgradeInventory = upgrades(device())

    override fun configuration(): Map<String, Any> = mapOf("upgradeSlotCount" to upgradeInventory().size())

    protected fun pullUpgrade(
        access: IComputerAccess,
        attached: (() -> Boolean)?,
        fromName: String,
        fromSlot: Int,
        limit: Optional<Int>,
        toSlot: Optional<Int>,
    ): Int {
        requireAttached(access, attached)
        return pullItem(level, access, upgradeInventory(), fromName, fromSlot, limit, toSlot) { true }
    }

    protected fun pushUpgrade(
        access: IComputerAccess,
        attached: (() -> Boolean)?,
        toName: String,
        fromSlot: Int,
        limit: Optional<Int>,
        toSlot: Optional<Int>,
    ): Int {
        requireAttached(access, attached)
        val target = device()
        val inventory = upgrades(target)
        val (moved, stack) = pushItem(level, access, inventory, toName, fromSlot, limit, toSlot)
        if (moved > 0 && stack.`is`(AEItems.CAPACITY_CARD.asItem())) {
            config?.invoke(target)?.let {
                clearInactiveFilters(inventory, it)
            }
        }
        return moved
    }

    @LuaFunction(mainThread = true)
    fun listUpgrades(): Map<Int, Map<String, Any>> = buildMap {
        val inventory = upgradeInventory()
        for (slot in 0 until inventory.size()) {
            val stack = inventory.getStackInSlot(slot)
            if (!stack.isEmpty) put(slot + 1, itemDetails(stack))
        }
    }

    @LuaFunction(mainThread = true)
    fun getUpgrade(slot: Int): Map<String, Any>? {
        val inventory = upgradeInventory()
        assertBetween(slot, 1, inventory.size(), "slot")
        return inventory.getStackInSlot(slot - 1).takeUnless(ItemStack::isEmpty)?.let(::itemDetails)
    }
}

internal abstract class FilterDeviceObject<T : Any>(
    deviceType: String,
    level: Level,
    resolveDevice: () -> T,
    private val filterUpgrades: (T) -> IUpgradeInventory,
    private val filter: (T) -> ConfigInventory,
) : UpgradeableDeviceObject<T>(deviceType, level, resolveDevice, filterUpgrades, filter) {
    private fun activeSlots(target: T): Int = min(
        18 + filterUpgrades(target).getInstalledUpgrades(AEItems.CAPACITY_CARD) * 9,
        filter(target).size(),
    )

    private fun checkedFilter(slot: Int): Pair<ConfigInventory, Int> {
        val target = device()
        val size = activeSlots(target)
        assertBetween(slot, 1, size, "slot")
        return filter(target) to size
    }

    override fun configuration(): Map<String, Any> {
        val target = device()
        return super.configuration() + ("filterSlotCount" to activeSlots(target))
    }

    @LuaFunction(mainThread = true)
    fun listFilters(): Map<Int, Map<String, String>> {
        val target = device()
        val inventory = filter(target)
        return buildMap {
            for (slot in 0 until activeSlots(target)) {
                inventory.getKey(slot)?.let {
                    put(slot + 1, AE2Helper.keyToMap(it))
                }
            }
        }
    }

    @LuaFunction(mainThread = true)
    fun getFilter(slot: Int): Map<String, String>? = checkedFilter(slot).first.getKey(slot - 1)?.let(AE2Helper::keyToMap)

    @LuaFunction(mainThread = true)
    fun setFilter(slot: Int, resource: Map<*, *>) {
        val inventory = checkedFilter(slot).first
        val stack = AE2Helper.parseResource(resource, false)
        if (!inventory.isAllowed(stack.what())) throw LuaException("Resource is not supported by this device")
        inventory.setStack(slot - 1, stack)
    }

    @LuaFunction(mainThread = true)
    fun clearFilter(slot: Int) {
        checkedFilter(slot).first.setStack(slot - 1, null)
    }
}

internal open class InterfaceObject(level: Level, resolve: () -> InterfaceLogicHost) :
    UpgradeableDeviceObject<InterfaceLogicHost>("interface", level, resolve, InterfaceLogicHost::getUpgrades),
    IPeripheralPlugin {
    private val host: InterfaceLogicHost get() = device()

    @LuaFunction(mainThread = true)
    fun getPriority(): Int = host.priority

    @LuaFunction(mainThread = true)
    fun setPriority(priority: Int) {
        host.priority = priority
    }

    @LuaFunction(mainThread = true)
    fun getFuzzyMode(): String {
        requireFuzzyCard(host.upgrades)
        return host.configManager.getSetting(Settings.FUZZY_MODE).name.lowercase(Locale.ROOT)
    }

    @LuaFunction(mainThread = true)
    fun setFuzzyMode(mode: String) {
        requireFuzzyCard(host.upgrades)
        host.configManager.putSetting(
            Settings.FUZZY_MODE,
            FuzzyMode.entries.firstOrNull { it.name.lowercase(Locale.ROOT) == mode }
                ?: throw invalidEnum("fuzzy mode", mode, FuzzyMode.entries.map { it.name.lowercase(Locale.ROOT) }),
        )
    }

    private fun stock(slot: Int): Pair<GenericStackInv, GenericStackInv> {
        assertBetween(slot, 1, 9, "slot")
        return host.interfaceLogic.config to host.interfaceLogic.storage
    }

    private fun stockRow(slot: Int): Map<String, Map<String, Any>>? {
        val (config, storage) = stock(slot)
        val target = config.getStack(slot - 1)
        val stored = storage.getStack(slot - 1)
        if (target == null && stored == null) return null
        return buildMap {
            target?.let { put("target", AE2Helper.stackToMap(it)) }
            stored?.let { put("stored", AE2Helper.stackToMap(it)) }
        }
    }

    @LuaFunction(mainThread = true)
    fun listStock(): Map<Int, Map<String, Map<String, Any>>> = buildMap {
        for (slot in 1..9) stockRow(slot)?.let { put(slot, it) }
    }

    @LuaFunction(mainThread = true)
    fun getStock(slot: Int): Map<String, Map<String, Any>>? = stockRow(slot)

    @LuaFunction(mainThread = true)
    fun setStock(slot: Int, target: Map<*, *>) {
        val config = stock(slot).first
        val stack = AE2Helper.parseResource(target, true)
        if (!config.isAllowed(stack.what())) throw LuaException("Resource is not supported by this interface")
        if (stack.amount() > config.getMaxAmount(stack.what())) throw LuaException("Stock amount is too large for this resource")
        config.setStack(slot - 1, stack)
    }

    @LuaFunction(mainThread = true)
    fun clearStock(slot: Int) {
        stock(slot).first.setStack(slot - 1, null)
    }
}

internal class DirectInterfaceObject(level: Level, entity: InterfaceBlockEntity) : InterfaceObject(level, { entity }) {
    @LuaFunction(mainThread = true)
    fun pullUpgrade(access: IComputerAccess, fromName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int = pullUpgrade(access, null, fromName, fromSlot, limit, toSlot)

    @LuaFunction(mainThread = true)
    fun pushUpgrade(access: IComputerAccess, toName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int = pushUpgrade(access, null, toName, fromSlot, limit, toSlot)
}

internal class SideInterfaceObject(
    level: Level,
    resolve: () -> InterfacePart,
    private val access: IComputerAccess,
    private val attached: () -> Boolean,
) : InterfaceObject(level, resolve) {
    @LuaFunction(mainThread = true)
    fun pullUpgrade(fromName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int = pullUpgrade(access, attached, fromName, fromSlot, limit, toSlot)

    @LuaFunction(mainThread = true)
    fun pushUpgrade(toName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int = pushUpgrade(access, attached, toName, fromSlot, limit, toSlot)
}

internal abstract class BusObject<T : IOBusPart>(
    deviceType: String,
    level: Level,
    resolve: () -> T,
) : FilterDeviceObject<T>(deviceType, level, resolve, IOBusPart::getUpgrades, IOBusPart::getConfig) {
    @LuaFunction(mainThread = true)
    fun getFuzzyMode(): String {
        val target = device()
        requireFuzzyCard(target.upgrades)
        return target.configManager.getSetting(Settings.FUZZY_MODE).name.lowercase(Locale.ROOT)
    }

    @LuaFunction(mainThread = true)
    fun setFuzzyMode(mode: String) {
        val target = device()
        requireFuzzyCard(target.upgrades)
        target.configManager.putSetting(
            Settings.FUZZY_MODE,
            FuzzyMode.entries.firstOrNull { it.name.lowercase(Locale.ROOT) == mode }
                ?: throw invalidEnum("fuzzy mode", mode, FuzzyMode.entries.map { it.name.lowercase(Locale.ROOT) }),
        )
    }

    @LuaFunction(mainThread = true)
    fun getRedstoneMode(): String = device().configManager.getSetting(Settings.REDSTONE_CONTROLLED).name.lowercase(Locale.ROOT)

    @LuaFunction(mainThread = true)
    fun setRedstoneMode(mode: String) = device().configManager.putSetting(
        Settings.REDSTONE_CONTROLLED,
        RedstoneMode.entries.firstOrNull { it.name.lowercase(Locale.ROOT) == mode }
            ?: throw invalidEnum("redstone mode", mode, RedstoneMode.entries.map { it.name.lowercase(Locale.ROOT) }),
    )
}

internal class ImportBusObject(
    level: Level,
    resolve: () -> ImportBusPart,
    private val access: IComputerAccess,
    private val attached: () -> Boolean,
) : BusObject<ImportBusPart>("import_bus", level, resolve) {
    @LuaFunction(mainThread = true)
    fun pullUpgrade(fromName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int = pullUpgrade(access, attached, fromName, fromSlot, limit, toSlot)

    @LuaFunction(mainThread = true)
    fun pushUpgrade(toName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int = pushUpgrade(access, attached, toName, fromSlot, limit, toSlot)
}

internal class ExportBusObject(
    level: Level,
    resolve: () -> ExportBusPart,
    private val access: IComputerAccess,
    private val attached: () -> Boolean,
) : BusObject<ExportBusPart>("export_bus", level, resolve) {
    @LuaFunction(mainThread = true)
    fun pullUpgrade(fromName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int = pullUpgrade(access, attached, fromName, fromSlot, limit, toSlot)

    @LuaFunction(mainThread = true)
    fun pushUpgrade(toName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int = pushUpgrade(access, attached, toName, fromSlot, limit, toSlot)

    @LuaFunction(mainThread = true)
    fun isCraftOnly(): Boolean = device().configManager.getSetting(Settings.CRAFT_ONLY) == YesNo.YES

    @LuaFunction(mainThread = true)
    fun setCraftOnly(craftOnly: Boolean) = device().configManager.putSetting(Settings.CRAFT_ONLY, if (craftOnly) YesNo.YES else YesNo.NO)

    @LuaFunction(mainThread = true)
    fun getSchedulingMode(): String = when (device().configManager.getSetting(Settings.SCHEDULING_MODE)) {
        SchedulingMode.DEFAULT -> "default"
        SchedulingMode.ROUNDROBIN -> "round_robin"
        SchedulingMode.RANDOM -> "random"
    }

    @LuaFunction(mainThread = true)
    fun setSchedulingMode(mode: String) = device().configManager.putSetting(
        Settings.SCHEDULING_MODE,
        when (mode) {
            "default" -> SchedulingMode.DEFAULT
            "round_robin" -> SchedulingMode.ROUNDROBIN
            "random" -> SchedulingMode.RANDOM
            else -> throw invalidEnum("scheduling mode", mode, listOf("default", "round_robin", "random"))
        },
    )
}

internal abstract class PriorityFilterObject<T : UpgradeablePart>(
    deviceType: String,
    level: Level,
    resolve: () -> T,
    filter: (T) -> ConfigInventory,
) : FilterDeviceObject<T>(deviceType, level, resolve, UpgradeablePart::getUpgrades, filter) {
    @LuaFunction(mainThread = true)
    fun getPriority(): Int = (device() as IPriorityHost).priority

    @LuaFunction(mainThread = true)
    fun setPriority(priority: Int) {
        (device() as IPriorityHost).priority = priority
    }

    @LuaFunction(mainThread = true)
    fun getFuzzyMode(): String {
        val target = device()
        requireFuzzyCard(target.upgrades)
        return target.configManager.getSetting(Settings.FUZZY_MODE).name.lowercase(Locale.ROOT)
    }

    @LuaFunction(mainThread = true)
    fun setFuzzyMode(mode: String) {
        val target = device()
        requireFuzzyCard(target.upgrades)
        target.configManager.putSetting(
            Settings.FUZZY_MODE,
            FuzzyMode.entries.firstOrNull { it.name.lowercase(Locale.ROOT) == mode }
                ?: throw invalidEnum("fuzzy mode", mode, FuzzyMode.entries.map { it.name.lowercase(Locale.ROOT) }),
        )
    }
}

internal class StorageBusObject(
    level: Level,
    resolve: () -> StorageBusPart,
    private val access: IComputerAccess,
    private val attached: () -> Boolean,
) : PriorityFilterObject<StorageBusPart>("storage_bus", level, resolve, StorageBusPart::getConfig) {
    @LuaFunction(mainThread = true)
    fun pullUpgrade(fromName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int = pullUpgrade(access, attached, fromName, fromSlot, limit, toSlot)

    @LuaFunction(mainThread = true)
    fun pushUpgrade(toName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int = pushUpgrade(access, attached, toName, fromSlot, limit, toSlot)

    @LuaFunction(mainThread = true)
    fun getAccessMode(): String = device().configManager.getSetting(Settings.ACCESS).name.lowercase(Locale.ROOT)

    @LuaFunction(mainThread = true)
    fun setAccessMode(mode: String) = device().configManager.putSetting(
        Settings.ACCESS,
        AccessRestriction.entries.firstOrNull { it.name.lowercase(Locale.ROOT) == mode }
            ?: throw invalidEnum("access mode", mode, AccessRestriction.entries.map { it.name.lowercase(Locale.ROOT) }),
    )

    @LuaFunction(mainThread = true)
    fun getStorageFilterMode(): String = device().configManager.getSetting(Settings.STORAGE_FILTER).name.lowercase(Locale.ROOT)

    @LuaFunction(mainThread = true)
    fun setStorageFilterMode(mode: String) = device().configManager.putSetting(
        Settings.STORAGE_FILTER,
        StorageFilter.entries.firstOrNull { it.name.lowercase(Locale.ROOT) == mode }
            ?: throw invalidEnum("storage filter mode", mode, StorageFilter.entries.map { it.name.lowercase(Locale.ROOT) }),
    )

    @LuaFunction(mainThread = true)
    fun shouldFilterOnExtract(): Boolean = device().configManager.getSetting(Settings.FILTER_ON_EXTRACT) == YesNo.YES

    @LuaFunction(mainThread = true)
    fun setFilterOnExtract(filterOnExtract: Boolean) = device().configManager.putSetting(Settings.FILTER_ON_EXTRACT, if (filterOnExtract) YesNo.YES else YesNo.NO)
}

internal class FormationPlaneObject(
    level: Level,
    resolve: () -> FormationPlanePart,
    private val access: IComputerAccess,
    private val attached: () -> Boolean,
) : PriorityFilterObject<FormationPlanePart>("formation_plane", level, resolve, FormationPlanePart::getConfig) {
    @LuaFunction(mainThread = true)
    fun pullUpgrade(fromName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int = pullUpgrade(access, attached, fromName, fromSlot, limit, toSlot)

    @LuaFunction(mainThread = true)
    fun pushUpgrade(toName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int = pushUpgrade(access, attached, toName, fromSlot, limit, toSlot)

    @LuaFunction(mainThread = true)
    fun shouldPlaceBlocks(): Boolean = device().configManager.getSetting(Settings.PLACE_BLOCK) == YesNo.YES

    @LuaFunction(mainThread = true)
    fun setPlaceBlocks(placeBlocks: Boolean) = device().configManager.putSetting(Settings.PLACE_BLOCK, if (placeBlocks) YesNo.YES else YesNo.NO)
}

internal abstract class LevelEmitterObject<T : AbstractLevelEmitterPart>(
    deviceType: String,
    level: Level,
    resolve: () -> T,
) : DeviceObject<T>(deviceType, level, resolve) {
    @LuaFunction(mainThread = true)
    fun getEmitterMode(): String = device().configManager.getSetting(Settings.REDSTONE_EMITTER).name.lowercase(Locale.ROOT)

    @LuaFunction(mainThread = true)
    fun setEmitterMode(mode: String) = device().configManager.putSetting(
        Settings.REDSTONE_EMITTER,
        when (mode) {
            "low_signal" -> RedstoneMode.LOW_SIGNAL
            "high_signal" -> RedstoneMode.HIGH_SIGNAL
            else -> throw invalidEnum("emitter mode", mode, listOf("low_signal", "high_signal"))
        },
    )

    @LuaFunction(mainThread = true)
    fun isEmitting(): Boolean = device().isProvidingWeakPower > 0
}

internal class StorageLevelEmitterObject(
    level: Level,
    resolve: () -> StorageLevelEmitterPart,
    private val access: IComputerAccess,
    private val attached: () -> Boolean,
) : LevelEmitterObject<StorageLevelEmitterPart>("storage_level_emitter", level, resolve) {
    override fun configuration(): Map<String, Any> = mapOf("upgradeSlotCount" to device().upgrades.size())

    private fun internalThreshold(threshold: Long): Long {
        if (threshold < 0) throw LuaException("Threshold must be a non-negative integer")
        val key = device().config.getKey(0)
        if (key !is AEFluidKey) return threshold
        val divider = PlatformToolkit.get().fluidCompactDivider.toLong()
        if (threshold > Long.MAX_VALUE / divider) throw LuaException("Threshold is too large")
        return threshold * divider
    }

    @LuaFunction(mainThread = true)
    fun listUpgrades(): Map<Int, Map<String, Any>> = buildMap {
        val inventory = device().upgrades
        for (slot in 0 until inventory.size()) {
            inventory.getStackInSlot(slot).takeUnless(ItemStack::isEmpty)?.let {
                put(slot + 1, itemDetails(it))
            }
        }
    }

    @LuaFunction(mainThread = true)
    fun getUpgrade(slot: Int): Map<String, Any>? {
        val inventory = device().upgrades
        assertBetween(slot, 1, inventory.size(), "slot")
        return inventory.getStackInSlot(slot - 1).takeUnless(ItemStack::isEmpty)?.let(::itemDetails)
    }

    @LuaFunction(mainThread = true)
    fun pullUpgrade(fromName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int {
        requireAttached(access, attached)
        return pullItem(level, access, device().upgrades, fromName, fromSlot, limit, toSlot) { true }
    }

    @LuaFunction(mainThread = true)
    fun pushUpgrade(toName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int {
        requireAttached(access, attached)
        return pushItem(level, access, device().upgrades, toName, fromSlot, limit, toSlot).first
    }

    @LuaFunction(mainThread = true)
    fun getFuzzyMode(): String {
        val target = device()
        requireFuzzyCard(target.upgrades)
        return target.configManager.getSetting(Settings.FUZZY_MODE).name.lowercase(Locale.ROOT)
    }

    @LuaFunction(mainThread = true)
    fun setFuzzyMode(mode: String) {
        val target = device()
        requireFuzzyCard(target.upgrades)
        target.configManager.putSetting(
            Settings.FUZZY_MODE,
            FuzzyMode.entries.firstOrNull { it.name.lowercase(Locale.ROOT) == mode }
                ?: throw invalidEnum("fuzzy mode", mode, FuzzyMode.entries.map { it.name.lowercase(Locale.ROOT) }),
        )
    }

    @LuaFunction(mainThread = true)
    fun getMonitoredResource(): Map<String, String>? = device().config.getKey(0)?.let(AE2Helper::keyToMap)

    @LuaFunction(mainThread = true)
    fun setMonitoredResource(resource: Map<*, *>) {
        val target = device()
        val stack = AE2Helper.parseResource(resource, false)
        if (!target.config.isAllowed(stack.what())) throw LuaException("Resource is not supported by this emitter")
        target.config.setStack(0, stack)
    }

    @LuaFunction(mainThread = true)
    fun clearMonitoredResource() = device().config.setStack(0, null)

    @LuaFunction(mainThread = true)
    fun getThreshold(): Long {
        val target = device()
        return AE2Helper.publicAmount(target.config.getKey(0) ?: return target.reportingValue, target.reportingValue)
    }

    @LuaFunction(mainThread = true)
    fun setThreshold(threshold: Long) = device().setReportingValue(internalThreshold(threshold))

    @LuaFunction(mainThread = true)
    fun getThresholdUnit(): String = when (device().config.getKey(0)) {
        null -> "ae_internal"
        is AEFluidKey -> "millibucket"
        else -> "item"
    }

    @LuaFunction(mainThread = true)
    fun shouldCraftViaRedstone(): Boolean = device().configManager.getSetting(Settings.CRAFT_VIA_REDSTONE) == YesNo.YES

    @LuaFunction(mainThread = true)
    fun setCraftViaRedstone(craftViaRedstone: Boolean) = device().configManager.putSetting(Settings.CRAFT_VIA_REDSTONE, if (craftViaRedstone) YesNo.YES else YesNo.NO)
}

internal class EnergyLevelEmitterObject(level: Level, resolve: () -> EnergyLevelEmitterPart) : LevelEmitterObject<EnergyLevelEmitterPart>("energy_level_emitter", level, resolve) {
    @LuaFunction(mainThread = true)
    fun getThreshold(): Long = device().reportingValue

    @LuaFunction(mainThread = true)
    fun setThreshold(threshold: Long) {
        if (threshold < 0) throw LuaException("Threshold must be a non-negative integer")
        device().reportingValue = threshold
    }
}

internal open class PatternProviderObject(level: Level, resolve: () -> PatternProviderLogicHost) :
    DeviceObject<PatternProviderLogicHost>("pattern_provider", level, resolve),
    IPeripheralPlugin {
    protected fun patternInventory(): InternalInventory = device().logic.patternInv

    protected fun pullPattern(access: IComputerAccess, attached: (() -> Boolean)?, fromName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int {
        requireAttached(access, attached)
        return pullItem(level, access, patternInventory(), fromName, fromSlot, limit, toSlot) {
            PatternDetailsHelper.decodePattern(it, level) != null
        }
    }

    protected fun pushPattern(access: IComputerAccess, attached: (() -> Boolean)?, toName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int {
        requireAttached(access, attached)
        return pushItem(level, access, patternInventory(), toName, fromSlot, limit, toSlot).first
    }

    @LuaFunction(mainThread = true)
    fun listPatterns(): Map<Int, Map<String, Any>> = buildMap {
        val inventory = patternInventory()
        for (slot in 0 until inventory.size()) {
            inventory.getStackInSlot(slot).takeUnless(ItemStack::isEmpty)?.let {
                put(slot + 1, itemDetails(it))
            }
        }
    }

    @LuaFunction(mainThread = true)
    fun getPattern(slot: Int): Map<String, Any>? {
        val inventory = patternInventory()
        assertBetween(slot, 1, inventory.size(), "slot")
        return inventory.getStackInSlot(slot - 1).takeUnless(ItemStack::isEmpty)?.let(::itemDetails)
    }

    @LuaFunction(mainThread = true)
    fun getPriority(): Int = device().priority

    @LuaFunction(mainThread = true)
    fun setPriority(priority: Int) {
        device().priority = priority
    }

    @LuaFunction(mainThread = true)
    fun isBlocking(): Boolean = device().configManager.getSetting(Settings.BLOCKING_MODE) == YesNo.YES

    @LuaFunction(mainThread = true)
    fun setBlocking(blocking: Boolean) = device().configManager.putSetting(Settings.BLOCKING_MODE, if (blocking) YesNo.YES else YesNo.NO)

    @LuaFunction(mainThread = true)
    fun isVisibleInPatternAccessTerminal(): Boolean = device().configManager.getSetting(Settings.PATTERN_ACCESS_TERMINAL) == YesNo.YES

    @LuaFunction(mainThread = true)
    fun setVisibleInPatternAccessTerminal(visible: Boolean) = device().configManager.putSetting(Settings.PATTERN_ACCESS_TERMINAL, if (visible) YesNo.YES else YesNo.NO)

    @LuaFunction(mainThread = true)
    fun getPatternLockMode(): String = device().configManager.getSetting(Settings.LOCK_CRAFTING_MODE).name.lowercase(Locale.ROOT)

    @LuaFunction(mainThread = true)
    fun setPatternLockMode(mode: String) = device().configManager.putSetting(
        Settings.LOCK_CRAFTING_MODE,
        LockCraftingMode.entries.firstOrNull { it.name.lowercase(Locale.ROOT) == mode }
            ?: throw invalidEnum("pattern lock mode", mode, LockCraftingMode.entries.map { it.name.lowercase(Locale.ROOT) }),
    )
}

internal class DirectPatternProviderObject(
    level: Level,
    private val entity: PatternProviderBlockEntity,
) : PatternProviderObject(level, { entity }) {
    @LuaFunction(mainThread = true)
    fun pullPattern(access: IComputerAccess, fromName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int = pullPattern(access, null, fromName, fromSlot, limit, toSlot)

    @LuaFunction(mainThread = true)
    fun pushPattern(access: IComputerAccess, toName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int = pushPattern(access, null, toName, fromSlot, limit, toSlot)

    @LuaFunction(mainThread = true)
    fun getPushDirection(): String = entity.blockState.getValue(PatternProviderBlock.PUSH_DIRECTION).serializedName

    @LuaFunction(mainThread = true)
    fun setPushDirection(direction: String) {
        val value = PushDirection.entries.firstOrNull { it.serializedName == direction }
            ?: throw invalidEnum("push direction", direction, PushDirection.entries.map(PushDirection::getSerializedName))
        level.setBlockAndUpdate(entity.blockPos, entity.blockState.setValue(PatternProviderBlock.PUSH_DIRECTION, value))
    }
}

internal class SidePatternProviderObject(
    level: Level,
    resolve: () -> PatternProviderPart,
    private val access: IComputerAccess,
    private val attached: () -> Boolean,
) : PatternProviderObject(level, resolve) {
    @LuaFunction(mainThread = true)
    fun pullPattern(fromName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int = pullPattern(access, attached, fromName, fromSlot, limit, toSlot)

    @LuaFunction(mainThread = true)
    fun pushPattern(toName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int = pushPattern(access, attached, toName, fromSlot, limit, toSlot)
}

private fun isSupportedCablePart(part: Any?): Boolean = part is InterfacePart ||
    part is ImportBusPart ||
    part is ExportBusPart ||
    part is StorageBusPart ||
    part is FormationPlanePart ||
    part is StorageLevelEmitterPart ||
    part is EnergyLevelEmitterPart ||
    part is PatternProviderPart

internal class CableConfigurableObject(private val level: Level, private val pos: BlockPos) : IPeripheralPlugin {
    override var connectedPeripheral: IExpandedPeripheral? = null

    private fun <T : Any> resolve(side: Direction, expected: Class<T>): T {
        val cable = level.getBlockEntity(pos) as? CableBusBlockEntity
            ?: throw LuaException("AE2 cable is no longer present")
        val part = cable.getPart(side)
        if (part == null || part.javaClass != expected) throw LuaException("AE2 ${side.serializedName} part is no longer the expected device")
        return expected.cast(part)
    }

    @LuaFunction(mainThread = true)
    fun getSides(): List<String> {
        val cable = level.getBlockEntity(pos) as? CableBusBlockEntity
            ?: throw LuaException("AE2 cable is no longer present")
        return Direction.entries.filter { isSupportedCablePart(cable.getPart(it)) }.map(Direction::getSerializedName)
    }

    @LuaFunction(mainThread = true)
    fun getSide(access: IComputerAccess, side: String): MethodResult {
        val direction = parseDirection(side)
        val cable = level.getBlockEntity(pos) as? CableBusBlockEntity
            ?: return MethodResult.of(null, "AE2 cable is no longer present")
        val part = cable.getPart(direction) ?: return MethodResult.of(null, "No AE2 part on side '$side'")
        val attached = { connectedPeripheral?.isComputerPresent(access.id) == true }
        val result: Any = when (part.javaClass) {
            InterfacePart::class.java -> SideInterfaceObject(level, { resolve(direction, InterfacePart::class.java) }, access, attached)
            ImportBusPart::class.java -> ImportBusObject(level, { resolve(direction, ImportBusPart::class.java) }, access, attached)
            ExportBusPart::class.java -> ExportBusObject(level, { resolve(direction, ExportBusPart::class.java) }, access, attached)
            StorageBusPart::class.java -> StorageBusObject(level, { resolve(direction, StorageBusPart::class.java) }, access, attached)
            FormationPlanePart::class.java -> FormationPlaneObject(level, { resolve(direction, FormationPlanePart::class.java) }, access, attached)
            StorageLevelEmitterPart::class.java -> StorageLevelEmitterObject(level, { resolve(direction, StorageLevelEmitterPart::class.java) }, access, attached)
            EnergyLevelEmitterPart::class.java -> EnergyLevelEmitterObject(level, { resolve(direction, EnergyLevelEmitterPart::class.java) })
            PatternProviderPart::class.java -> SidePatternProviderObject(level, { resolve(direction, PatternProviderPart::class.java) }, access, attached)
            else -> return MethodResult.of(null, "Unsupported AE2 part on side '$side'")
        }
        return MethodResult.of(result)
    }
}

object AE2CableObjectProvider : PeripheralPluginProvider {
    override val pluginType = "ae2_cable_objects"
    override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
        val cable = level.getBlockEntity(pos) as? CableBusBlockEntity ?: return null
        return CableConfigurableObject(level, pos).takeIf { Direction.entries.any { direction -> isSupportedCablePart(cable.getPart(direction)) } }
    }
}

object AE2InterfaceObjectProvider : PeripheralPluginProvider {
    override val pluginType = "ae2_interface_object"
    override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? = (level.getBlockEntity(pos) as? InterfaceBlockEntity)?.let { DirectInterfaceObject(level, it) }
}

object AE2PatternProviderObjectProvider : PeripheralPluginProvider {
    override val pluginType = "ae2_pattern_provider_object"
    override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? = (level.getBlockEntity(pos) as? PatternProviderBlockEntity)?.let { DirectPatternProviderObject(level, it) }
}
