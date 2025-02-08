package site.siredvin.peripheralworks.computercraft.peripherals

import site.siredvin.peripheralium.api.peripheral.IPeripheralOwner
import site.siredvin.peripheralium.computercraft.peripheral.OwnedPeripheral

class PluggablePeripheral<O : IPeripheralOwner>(peripheralType: String, peripheralOwner: O) : OwnedPeripheral<O>(peripheralType, peripheralOwner) {
    override val isEnabled: Boolean
        get() = true
}
