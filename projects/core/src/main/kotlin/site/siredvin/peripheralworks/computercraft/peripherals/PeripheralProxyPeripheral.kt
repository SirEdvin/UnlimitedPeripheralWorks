package site.siredvin.peripheralworks.computercraft.peripherals

import site.siredvin.peripheralium.computercraft.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.peripheralworks.common.blockentity.PeripheralProxyBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.modem.ModemPeripheral

class PeripheralProxyPeripheral(private val blockEntity: PeripheralProxyBlockEntity) :
    ModemPeripheral<BlockEntityPeripheralOwner<PeripheralProxyBlockEntity>>(TYPE, BlockEntityPeripheralOwner(blockEntity)) {

    companion object {
        const val TYPE = "peripheral_proxy"
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enablePeripheralProxy

    override fun getAdditionalTypes(): Set<String> {
        return setOf("peripheral_hub")
    }
}
