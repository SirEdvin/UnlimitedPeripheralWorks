package site.siredvin.peripheralworks.xplat

import site.siredvin.peripheralium.storages.energy.EnergyUnit
import site.siredvin.peripheralium.xplat.BaseInnerPlatform

interface ModInnerPlatform : BaseInnerPlatform {
    val commonEnergy: EnergyUnit
}
