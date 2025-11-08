package site.siredvin.peripheralworks.computercraft.peripherals

import net.minecraft.core.Direction
import site.siredvin.tweakium.modules.peripheral.OwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.api.ISidedPeripheral

class PluggablePeripheral<O : IPeripheralOwner>(
    peripheralType: String,
    peripheralOwner: O,
    override val side: Direction?,
) : OwnedPeripheral<O>(peripheralType, peripheralOwner, "upw"),
    ISidedPeripheral {
    override val isEnabled: Boolean
        get() = true
}
