package site.siredvin.peripheralworks.computercraft.modem

import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.upgrades.UpgradeBase
import dan200.computercraft.api.upgrades.UpgradeData

interface LocalWrapper<T : UpgradeBase> {
    val id: String
        get() = fullUpgrade.holder.key().location().toString()
    val peripheral: IPeripheral?
    val fullUpgrade: UpgradeData<T>
}
