package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import site.siredvin.peripheralium.computercraft.peripheral.OwnedPeripheral
import site.siredvin.peripheralium.computercraft.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.peripheralium.util.representation.LuaRepresentation
import site.siredvin.peripheralworks.common.blockentity.EntityLinkBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.subsystem.entityperipheral.EntityPeripheralLookup

class EntityLinkPeripheral(private val blockEntity: EntityLinkBlockEntity) : OwnedPeripheral<BlockEntityPeripheralOwner<EntityLinkBlockEntity>>(TYPE, BlockEntityPeripheralOwner(blockEntity)) {
    companion object {
        const val TYPE = "entity_link"
    }

    private var _configured = false

    val configured: Boolean
        get() = _configured

    init {
        recollectPlugins()
    }

    fun recollectPlugins() {
        _configured = false
        plugins?.clear()
        initialized = false
        if (blockEntity.entity != null) {
            val plugins = EntityPeripheralLookup.collectPlugins(blockEntity.entity!!)
            plugins.values.forEach(::addPlugin)
        }
        buildPlugins()
        _configured = blockEntity.level is ServerLevel
    }

    @LuaFunction(mainThread = true)
    fun isEntityFound(): Boolean {
        return blockEntity.entity != null
    }

    @LuaFunction(mainThread = true)
    fun inspect(): Map<String, Any>? {
        val entity = blockEntity.entity ?: return null
        if (entity is LivingEntity) {
            return LuaRepresentation.withPos(entity, blockEntity.facing, blockEntity.blockPos, LuaRepresentation::forLivingEntity)
        }
        return LuaRepresentation.withPos(entity, blockEntity.facing, blockEntity.blockPos, LuaRepresentation::forEntity)
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + blockEntity.hashCode()
        return result
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enableEntityLink


}
