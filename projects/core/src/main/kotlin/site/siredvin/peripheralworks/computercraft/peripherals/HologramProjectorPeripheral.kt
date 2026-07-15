package site.siredvin.peripheralworks.computercraft.peripherals

import com.google.gson.Gson
import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.FloatTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.TagParser
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.AABB
import site.siredvin.broccolium.modules.base.ext.toBlockPos
import site.siredvin.broccolium.modules.base.ext.toVec3
import site.siredvin.broccolium.modules.platform.PlatformRegistries
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.utils.getVec
import site.siredvin.tweakium.modules.peripheral.OwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IDataStorage
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation
import site.siredvin.tweakium.modules.peripheral.util.AbstractNotNullDataObject
import java.util.*
import kotlin.collections.contains
import kotlin.collections.get

class HologramProjectorPeripheral(owner: IPeripheralOwner) : OwnedPeripheral<IPeripheralOwner>(TYPE, owner) {
    companion object {
        const val TYPE = "hologram_projector"

        val UPGRADE_ID = ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, TYPE)

        object Entities : AbstractNotNullDataObject<MutableList<String>>() {
            override val nbtTag: String
                get() = "entities"
            override val default: MutableList<String>
                get() = mutableListOf()

            fun add(peripheralOwner: IPeripheralOwner, value: String) {
                val list = get(peripheralOwner)
                list.add(value)
                set(peripheralOwner, list)
            }

            fun remove(peripheralOwner: IPeripheralOwner, value: String) {
                val list = get(peripheralOwner)
                list.remove(value)
                set(peripheralOwner, list)
            }

            override fun read(data: IDataStorage): MutableList<String> {
                val tag = data.getList(nbtTag, StringTag.TAG_STRING.toInt())
                return tag.map { (it as StringTag).asString }.toMutableList()
            }

            override fun write(
                data: IDataStorage,
                value: MutableList<String>,
            ): Boolean {
                val tag = ListTag()
                value.forEach { tag.add(StringTag.valueOf(it)) }
                data.putList(this.nbtTag, tag)
                return true
            }
        }
        val GSON = Gson()

        // transformation
        val INT_OPTIONS = setOf("brightness", "glow_color_override", "interpolation_duration", "start_interpolation")
        val FLOAT_OPTIONS = setOf("height", "width", "shadow_radius", "shadow_strength", "view_range")
        val STR_OPTIONS = setOf("billboard")
        val TEXT_INT_OPTIONS = setOf("line_width", "background")
        val TEXT_BOOLEAN_OPTIONS = setOf("shadow", "see_through", "default_background")
        val TEXT_BYTE_OPTIONS = setOf("text_opacity")
        val TEXT_STR_OPTIONS = setOf("alignment")
        val ITEM_STR_OPTIONS = setOf("item_display")
        val ROTATION_DEFAULT by lazy {
            val lst = ListTag()
            lst.add(FloatTag.valueOf(0.0f))
            lst.add(FloatTag.valueOf(0.0f))
            lst.add(FloatTag.valueOf(0.0f))
            lst.add(FloatTag.valueOf(1.0f))
            return@lazy lst
        }
        val SCALE_DEFAULT by lazy {
            val lst = ListTag()
            lst.add(FloatTag.valueOf(1.0f))
            lst.add(FloatTag.valueOf(1.0f))
            lst.add(FloatTag.valueOf(1.0f))
            return@lazy lst
        }
        val TRANSLATE_DEFAULT by lazy {
            val lst = ListTag()
            lst.add(FloatTag.valueOf(0.0f))
            lst.add(FloatTag.valueOf(0.0f))
            lst.add(FloatTag.valueOf(0.0f))
            return@lazy lst
        }
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enableHologramProjector

    override val peripheralConfiguration: MutableMap<String, Any>
        get() {
            val base = super.peripheralConfiguration
            base["entityLimit"] = PeripheralWorksConfig.hologramProjectorEntityLimit
            base["distanceLimit"] = PeripheralWorksConfig.hologramProjectorDistanceLimit
            return base
        }

    fun findEntity(uuid: String): Pair<Entity?, MethodResult?> {
        val level = peripheralOwner.level as? ServerLevel ?: return Pair(null, MethodResult.of(null, "Cannot locate level for some reason"))
        val ownerUUID = peripheralOwner.ownerUUID ?: return Pair(null, MethodResult.of(null, "Cannot locale owner"))
        val entity = level.getEntity(UUID.fromString(uuid))
        if (entity == null) {
            Entities.remove(peripheralOwner, uuid)
            return Pair(null, MethodResult.of(null, "Cannot find entity"))
        }
        if (entity !is Display) {
            Entities.remove(peripheralOwner, uuid)
            return Pair(null, MethodResult.of(null, "Target entity incorrect"))
        }
        if (!entity.tags.contains("spawner:$ownerUUID")) {
            Entities.remove(peripheralOwner, uuid)
            return Pair(null, MethodResult.of(null, "Target entity incorrect"))
        }
        return Pair(entity, null)
    }

    fun parseNumberList(name: String, data: Map<*, *>, expectedSize: Number, tag: CompoundTag, default: ListTag) {
        if (data.contains(name)) {
            val scale = data[name] as? Map<*, *> ?: throw LuaException("$name must be a table with $expectedSize elements")
            if (scale.size != expectedSize) {
                throw LuaException("$name must be a table with $expectedSize elements")
            }
            val scales = scale.values.map { it as? Number ?: throw LuaException("$name elements must be number") }.toList()
            val listTag = ListTag()
            scales.forEach { listTag.add(FloatTag.valueOf(it.toFloat())) }
            tag.put(name, listTag)
        } else {
            tag.put(name, default)
        }
    }

    fun handleDefaultOptions(it: Map.Entry<Any?, Any?>, data: CompoundTag) {
        if (INT_OPTIONS.contains(it.key)) {
            val number = it.value as? Number
            if (number != null) {
                data.putInt(it.key.toString(), number.toInt())
            }
        }
        if (FLOAT_OPTIONS.contains(it.key)) {
            val number = it.value as? Number
            if (number != null) {
                data.putFloat(it.key.toString(), number.toFloat())
            }
        }
        if (STR_OPTIONS.contains(it.key)) {
            data.putString(it.key.toString(), it.value.toString())
        }
        if (it.key == "transformation") {
            val value = it.value as? Map<*, *> ?: throw LuaException("Transformation should be a table")
            val innerTag = CompoundTag()
            parseNumberList("scale", value, 3, innerTag, SCALE_DEFAULT)
            parseNumberList("translation", value, 3, innerTag, TRANSLATE_DEFAULT)
            parseNumberList("left_rotation", value, 4, innerTag, ROTATION_DEFAULT)
            parseNumberList("right_rotation", value, 4, innerTag, ROTATION_DEFAULT)
            data.put("transformation", innerTag)
        }
    }

    fun mergeTextOptions(data: CompoundTag, options: Optional<Map<*, *>>) {
        if (options.isPresent) {
            options.get().forEach {
                handleDefaultOptions(it, data)
                if (TEXT_INT_OPTIONS.contains(it.key)) {
                    val number = it.value as? Number
                    if (number != null) {
                        data.putInt(it.key.toString(), number.toInt())
                    }
                }
                if (TEXT_STR_OPTIONS.contains(it.key)) {
                    data.putString(it.key.toString(), it.value.toString())
                }
                if (TEXT_BYTE_OPTIONS.contains(it.key)) {
                    val number = it.value as? Number
                    if (number != null) {
                        data.putByte(it.key.toString(), number.toByte())
                    }
                }
                if (TEXT_BOOLEAN_OPTIONS.contains(it.key)) {
                    val number = it.value as? Boolean
                    if (number != null) {
                        data.putBoolean(it.key.toString(), number)
                    }
                }
            }
        }
    }

    fun mergeItemOptions(data: CompoundTag, options: Optional<Map<*, *>>) {
        if (options.isPresent) {
            options.get().forEach {
                handleDefaultOptions(it, data)
                if (ITEM_STR_OPTIONS.contains(it.key)) {
                    data.putString(it.key.toString(), it.value.toString())
                }
            }
        }
    }

    fun mergeBlockOptions(data: CompoundTag, options: Optional<Map<*, *>>) {
        if (options.isPresent) {
            options.get().forEach {
                handleDefaultOptions(it, data)
            }
        }
    }

    fun spawnEntity(data: CompoundTag): MethodResult {
        if (Entities[peripheralOwner].size > PeripheralWorksConfig.hologramProjectorEntityLimit) {
            return MethodResult.of(null, "Cannot create more then 10 displays")
        }
        val ownerUUID = peripheralOwner.ownerUUID ?: return MethodResult.of(null, "Cannot locale owner")
        val level = peripheralOwner.level as? ServerLevel ?: return MethodResult.of(null, "Cannot locale level")
        val pos = peripheralOwner.pos
        val entity = EntityType.loadEntityRecursive(data, peripheralOwner.level!!) { entity ->
            entity.moveTo(pos.x.toDouble() + 0.5, pos.y.toDouble() + 1.5, pos.z.toDouble() + 0.5, entity.xRot, entity.yRot)
            entity.tags.add("spawner:$ownerUUID")
            return@loadEntityRecursive entity
        }
        if (entity != null) {
            Entities.add(peripheralOwner, entity.stringUUID)
            if (level.tryAddFreshEntityWithPassengers(entity)) {
                return MethodResult.of(entity.stringUUID)
            }
        }
        return MethodResult.of(null, "Something went wrong")
    }

    fun describeEntity(entity: Display): Map<String, Any> {
        val data = CompoundTag()
        entity.save(data)
        val subInfo = mutableMapOf<String, Any>(
            "type" to entity.type.descriptionId,
        )
        INT_OPTIONS.forEach { opt ->
            if (data.contains(opt)) {
                subInfo[opt] = data.getInt(opt)
            }
        }
        STR_OPTIONS.forEach { opt ->
            if (data.contains(opt)) {
                subInfo[opt] = data.getString(opt)
            }
        }
        when (entity) {
            is Display.TextDisplay -> {
                subInfo["text"] = data.getString("text")
                TEXT_INT_OPTIONS.forEach { opt ->
                    if (data.contains(opt)) {
                        subInfo[opt] = data.getInt(opt)
                    }
                }
                TEXT_STR_OPTIONS.forEach { opt ->
                    if (data.contains(opt)) {
                        subInfo[opt] = data.getString(opt)
                    }
                }
                TEXT_BYTE_OPTIONS.forEach { opt ->
                    if (data.contains(opt)) {
                        subInfo[opt] = data.getByte(opt)
                    }
                }
                TEXT_BOOLEAN_OPTIONS.forEach { opt ->
                    if (data.contains(opt)) {
                        subInfo[opt] = data.getBoolean(opt)
                    }
                }
            }

            is Display.ItemDisplay -> {
                val stack = ItemStack.parse(entity.level().registryAccess(), data.getCompound("item")).orElse(ItemStack.EMPTY)
                subInfo["item"] = LuaRepresentation.forItemStack(stack)
                subInfo["item_display"] = data.getString("item_display")
            }

            is Display.BlockDisplay -> {
                subInfo["block_state"] = LuaRepresentation.forBlockState(
                    NbtUtils.readBlockState(
                        PlatformRegistries.BLOCKS,
                        data.getCompound("block_state"),
                    ),
                )
            }
        }
        return subInfo
    }

    @LuaFunction(mainThread = true)
    fun destroy(uuid: String): MethodResult {
        val entityResult = findEntity(uuid)
        if (entityResult.second != null) {
            return entityResult.second!!
        }
        entityResult.first!!.setRemoved(Entity.RemovalReason.DISCARDED)
        Entities.remove(peripheralOwner, uuid)
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun move(arguments: IArguments): MethodResult {
        val uuid = arguments.getString(0)
        val pos = arguments.getVec(1, peripheralOwner.pos.toVec3().add(0.5, 0.5, 0.5), peripheralOwner.facing)
        val level = peripheralOwner.level ?: return MethodResult.of(null, "Cannot find level")
        if (!level.isLoaded(pos.toBlockPos())) {
            return MethodResult.of(null, "Cannot send entity into unloaded location")
        }
        val entityResult = findEntity(uuid)
        if (entityResult.second != null) {
            return entityResult.second!!
        }
        val entity = entityResult.first!!
        val distanceVector = pos.subtract(entity.position())
        if (distanceVector.length() > PeripheralWorksConfig.hologramProjectorDistanceLimit) {
            val modifiedPos = pos.normalize().multiply(PeripheralWorksConfig.hologramProjectorDistanceLimit, PeripheralWorksConfig.hologramProjectorDistanceLimit, PeripheralWorksConfig.hologramProjectorDistanceLimit)
            entity.moveTo(modifiedPos)
        } else {
            entity.moveTo(pos)
        }
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun rotate(arguments: IArguments): MethodResult {
        val uuid = arguments.getString(0)
        val entityResult = findEntity(uuid)
        if (entityResult.second != null) {
            return entityResult.second!!
        }
        val entity = entityResult.first!!
        val xRot = arguments.optDouble(1, entity.xRot.toDouble()).toFloat()
        val yRot = arguments.optDouble(2, entity.yRot.toDouble()).toFloat()
        entity.xRot = xRot
        entity.yRot = yRot
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun list(mode: String): MethodResult {
        val info = mutableMapOf<String, Map<String, Any>>()
        val level = peripheralOwner.level as? ServerLevel ?: return MethodResult.of(null, "Cannot locate level for some reason")
        val ownerUUID = peripheralOwner.ownerUUID ?: return MethodResult.of(null, "Cannot locale owner")
        val ownershipTag = "spawner:$ownerUUID"
        when (mode) {
            "owned" -> {
                Entities[peripheralOwner].forEach {
                    val entity = level.getEntity(UUID.fromString(it))
                    if (entity is Display && entity.tags.contains(ownershipTag)) {
                        info[it] = describeEntity(entity)
                    } else {
                        info[it] = emptyMap()
                    }
                }
            }
            "around" -> {
                level.getEntitiesOfClass(Display::class.java, AABB(peripheralOwner.pos).inflate(32.0)).forEach {
                    if (it.tags.contains(ownershipTag)) {
                        info[it.stringUUID] = describeEntity(it)
                    }
                }
            }
            else -> {
                throw LuaException("Incorrect mode, should be owned or around")
            }
        }
        return MethodResult.of(info)
    }

    @LuaFunction(mainThread = true)
    fun update(arguments: IArguments): MethodResult {
        val uuid = arguments.getString(0)
        val entityResult = findEntity(uuid)
        if (entityResult.second != null) {
            return entityResult.second!!
        }
        val entity = entityResult.first!!
        if (entity is Display.TextDisplay) {
            val text = arguments.optTable(1)
            val options = arguments.optTable(2)
            val data = CompoundTag()
            if (text.isPresent) {
                data.putString("text", GSON.toJson(text.get()))
            }
            mergeTextOptions(data, options)
            if (!data.isEmpty) {
                entity.readAdditionalSaveData(data)
            }
        } else if (entity is Display.BlockDisplay) {
            val block = arguments.optTable(1)
            val options = arguments.optTable(2)
            val data = CompoundTag()
            if (block.isPresent) {
                data.putString("block_state", GSON.toJson(block.get()))
            }
            mergeBlockOptions(data, options)
            if (!data.isEmpty) {
                entity.readAdditionalSaveData(data)
            }
        } else if (entity is Display.ItemDisplay) {
            val item = arguments.optTable(1)
            val options = arguments.optTable(2)
            val data = CompoundTag()
            if (item.isPresent) {
                data.putString("item", GSON.toJson(item.get()))
            }
            mergeItemOptions(data, options)
            if (!data.isEmpty) {
                entity.readAdditionalSaveData(data)
            }
        }
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun item(arguments: IArguments): MethodResult {
        val item = arguments.getTable(0)
        val options = arguments.optTable(1)
        val data = CompoundTag()
        data.put("item", TagParser.parseTag(GSON.toJson(item)))
        data.putString("id", "minecraft:item_display")
        mergeItemOptions(data, options)
        return spawnEntity(data)
    }

    @LuaFunction(mainThread = true)
    fun block(arguments: IArguments): MethodResult {
        val block = arguments.getTable(0)
        val options = arguments.optTable(1)
        val data = CompoundTag()
        data.put("block_state", TagParser.parseTag(GSON.toJson(block)))
        data.putString("id", "minecraft:block_display")
        mergeBlockOptions(data, options)
        return spawnEntity(data)
    }

    @LuaFunction(mainThread = true)
    fun text(arguments: IArguments): MethodResult {
        val text = arguments.getTable(0)
        val options = arguments.optTable(1)
        val data = CompoundTag()
        data.putString("text", GSON.toJson(text))
        data.putString("id", "minecraft:text_display")
        mergeTextOptions(data, options)
        return spawnEntity(data)
    }

    @LuaFunction(mainThread = true)
    fun ride(riderUUID: String, horseUUID: String): MethodResult {
        if (riderUUID == horseUUID) {
            return MethodResult.of(null, "Cannot ride itself")
        }
        val riderFind = findEntity(riderUUID)
        if (riderFind.second != null) {
            return riderFind.second!!
        }
        val horseFind = findEntity(horseUUID)
        if (horseFind.second != null) {
            return horseFind.second!!
        }
        val rider = riderFind.first!!
        val horse = horseFind.first!!
        if (rider.passengers.isNotEmpty()) {
            return MethodResult.of(null, "Rider can't have passengers")
        }
        val riding = rider.startRiding(horse, true)
        if (!riding) {
            return MethodResult.of(false, "Rider can't mount entity")
        }
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun unmount(riderUUID: String): MethodResult {
        val riderFind = findEntity(riderUUID)
        if (riderFind.second != null) {
            return riderFind.second!!
        }
        val rider = riderFind.first!!
        rider.stopRiding()
        return MethodResult.of(true)
    }
}
