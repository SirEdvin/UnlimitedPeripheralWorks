package site.siredvin.peripheralworks.xplat

import site.siredvin.broccolium.modules.storage.energy.EnergyUnit
import site.siredvin.tweakium.modules.platform.api.InnerComputerBasePlatform

interface ModInnerPlatform : InnerComputerBasePlatform {
    val commonEnergy: EnergyUnit
}
