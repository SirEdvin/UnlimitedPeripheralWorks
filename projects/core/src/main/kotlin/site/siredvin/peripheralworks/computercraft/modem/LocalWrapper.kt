package site.siredvin.peripheralworks.computercraft.modem

import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.upgrades.UpgradeBase
import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.core.Holder

interface LocalWrapper<T : UpgradeBase> {
    val id: String
    val peripheral: IPeripheral?
    val upgrade: Holder.Reference<T>
    val fullUpgradeData: UpgradeData<T>
}
