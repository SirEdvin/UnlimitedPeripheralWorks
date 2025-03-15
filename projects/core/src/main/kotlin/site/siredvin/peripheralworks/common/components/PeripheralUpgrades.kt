package site.siredvin.peripheralworks.common.components

import dan200.computercraft.api.upgrades.UpgradeBase
import dan200.computercraft.api.upgrades.UpgradeData

class PeripheralUpgrades<T : UpgradeBase>(val upgrades: List<UpgradeData<T>>) {

    constructor() : this(listOf())
}
