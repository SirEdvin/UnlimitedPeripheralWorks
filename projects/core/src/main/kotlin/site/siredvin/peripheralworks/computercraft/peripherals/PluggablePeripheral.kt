package site.siredvin.peripheralworks.computercraft.peripherals

import site.siredvin.tweakium.modules.peripheral.OwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralOwner

class PluggablePeripheral<O : IPeripheralOwner>(peripheralType: String, peripheralOwner: O) : OwnedPeripheral<O>(peripheralType, peripheralOwner, "upw") {
    override val isEnabled: Boolean
        get() = true
}
