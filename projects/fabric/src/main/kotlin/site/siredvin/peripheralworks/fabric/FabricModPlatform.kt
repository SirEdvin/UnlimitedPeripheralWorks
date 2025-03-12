package site.siredvin.peripheralworks.fabric

import site.siredvin.broccolium.modules.storage.energy.Energies
import site.siredvin.broccolium.modules.storage.energy.EnergyUnit
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.xplat.ModInnerPlatform
import site.siredvin.tweakium.modules.platform.FabricInnerComputerBasePlatform

object FabricModPlatform : FabricInnerComputerBasePlatform(), ModInnerPlatform {
    override val commonEnergy: EnergyUnit
        get() = Energies.REDSTONE_FLUX
    override val modID: String
        get() = PeripheralWorksCore.MOD_ID
}
