package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import site.siredvin.peripheralworks.common.blockentity.PeripheralProxyBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.modem.PeripheralHubPeripheral
import site.siredvin.peripheralworks.tags.BlockTags
import site.siredvin.tweakium.modules.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.representation.LuaInterpretation
import site.siredvin.tweakium.modules.platform.ComputerPlatformToolkit

class PeripheralProxyPeripheral(private val blockEntity: PeripheralProxyBlockEntity) : PeripheralHubPeripheral<BlockEntityPeripheralOwner<PeripheralProxyBlockEntity>>(TYPE, BlockEntityPeripheralOwner(blockEntity)) {

    companion object {
        const val TYPE = "peripheral_proxy"
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enablePeripheralProxy

    override fun getAdditionalTypes(): Set<String> = setOf("peripheral_hub")

    @LuaFunction(mainThread = true)
    fun getPositionRemote(name: String): Map<String, Any>? {
        val record = blockEntity.remotePeripherals.values.find { it.peripheralName == name } ?: return null
        return mapOf(
            "x" to record.targetBlock.x,
            "y" to record.targetBlock.y,
            "z" to record.targetBlock.z,
            "direction" to record.direction.serializedName,
        )
    }

    @LuaFunction(mainThread = true)
    fun addPeripheral(pos: Map<*, *>, direction: String): MethodResult {
        val targetPos = LuaInterpretation.asBlockPos(pos)
        val targetDirection = Direction.entries.find { it.serializedName == direction.lowercase() }
            ?: return MethodResult.of(false, "Direction should be one of: down, up, north, south, west, east")
        val level = peripheralOwner.level as? ServerLevel ?: return MethodResult.of(false, "Peripheral proxy is not in a server level")
        if (blockEntity.containsPos(targetPos)) return MethodResult.of(false, "Peripheral is already connected")
        if (!blockEntity.isPosApplicable(targetPos)) return MethodResult.of(false, "Position is the proxy itself or too far away")
        if (blockEntity.remotePeripherals.size >= PeripheralWorksConfig.peripheralProxyMaxCapacity) {
            return MethodResult.of(false, "Too many peripherals already connected")
        }
        if (!level.isLoaded(targetPos)) return MethodResult.of(false, "Target position is not loaded")
        if (level.getBlockState(targetPos).`is`(BlockTags.PERIPHERAL_PROXY_FORBIDDEN)) {
            return MethodResult.of(false, "This block is forbidden to add to peripheral proxy")
        }
        val targetPeripheral = ComputerPlatformToolkit.get().getPeripheral(level, targetPos, targetDirection)
            ?: return MethodResult.of(false, "This block does not contain a peripheral on the requested side")
        blockEntity.togglePos(targetPos, targetDirection, targetPeripheral)
        return MethodResult.of(true, blockEntity.remotePeripherals[targetPos]?.peripheralName)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PeripheralProxyPeripheral) return false
        if (!super.equals(other)) return false

        if (blockEntity != other.blockEntity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + blockEntity.hashCode()
        return result
    }
}
