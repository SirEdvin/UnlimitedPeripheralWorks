package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import site.siredvin.peripheralium.api.peripheral.IPeripheralOwner
import site.siredvin.peripheralium.computercraft.peripheral.OwnedPeripheral
import site.siredvin.peripheralium.util.representation.LuaRepresentation
import site.siredvin.peripheralworks.common.blockentity.EntityLinkBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.subsystem.entityperipheral.EntityPeripheralLookup
import java.util.function.BiConsumer

class EntityLinkPeripheral(private val blockEntity: EntityLinkBlockEntity, owner: IPeripheralOwner) : OwnedPeripheral<IPeripheralOwner>(TYPE, owner) {
    companion object {
        const val TYPE = "entity_link"
        val ENRICHERS: MutableList<BiConsumer<Entity, MutableMap<String, Any>>> = mutableListOf()

        init {
            ENRICHERS.add(
                BiConsumer<Entity, MutableMap<String, Any>> { entity, data ->
                    data["yRot"] = entity.yRot
                    data["xRot"] = entity.xRot
                },
            )
        }
    }

    private var _configured = false

    val configured: Boolean
        get() = initialized

    override fun buildPlugins() {
        plugins?.clear()
        if (blockEntity.entity != null) {
            val plugins = EntityPeripheralLookup.collectPlugins(blockEntity.entity!!)
            plugins.values.forEach(::addPlugin)
        }
        super.buildPlugins()
        initialized = blockEntity.level is ServerLevel
    }

    fun configure() {
        buildPlugins()
    }

    @LuaFunction(mainThread = true)
    fun isEntityFound(): Boolean {
        return blockEntity.entity != null
    }

    @LuaFunction(mainThread = true)
    fun inspect(): Map<String, Any>? {
        val entity = blockEntity.entity ?: return null
        val baseData = if (entity is LivingEntity) {
            LuaRepresentation.withPos(entity, blockEntity.facing, blockEntity.blockPos, LuaRepresentation::forLivingEntity)
        } else {
            LuaRepresentation.withPos(entity, blockEntity.facing, blockEntity.blockPos, LuaRepresentation::forEntity)
        }
        ENRICHERS.forEach {
            it.accept(entity, baseData)
        }
        return baseData
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + blockEntity.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EntityLinkPeripheral) return false
        if (!super.equals(other)) return false

        if (blockEntity != other.blockEntity) return false
        if (_configured != other._configured) return false

        return true
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enableEntityLink
}
