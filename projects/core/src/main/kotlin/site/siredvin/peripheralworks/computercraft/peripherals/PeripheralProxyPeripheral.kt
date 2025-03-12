package site.siredvin.peripheralworks.computercraft.peripherals

import site.siredvin.peripheralworks.common.blockentity.PeripheralProxyBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.modem.PeripheralHubPeripheral
import site.siredvin.tweakium.modules.peripheral.owner.BlockEntityPeripheralOwner

class PeripheralProxyPeripheral(private val blockEntity: PeripheralProxyBlockEntity) : PeripheralHubPeripheral<BlockEntityPeripheralOwner<PeripheralProxyBlockEntity>>(TYPE, BlockEntityPeripheralOwner(blockEntity)) {

    companion object {
        const val TYPE = "peripheral_proxy"
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enablePeripheralProxy

    override fun getAdditionalTypes(): Set<String> = setOf("peripheral_hub")

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
