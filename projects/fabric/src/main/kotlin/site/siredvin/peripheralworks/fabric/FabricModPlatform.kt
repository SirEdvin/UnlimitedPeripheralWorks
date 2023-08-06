package site.siredvin.peripheralworks.fabric

import site.siredvin.peripheralium.fabric.FabricBaseInnerPlatform
import site.siredvin.peripheralium.storages.energy.EnergyUnit
import site.siredvin.peripheralworks.Energies
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.xplat.ModInnerPlatform

object FabricModPlatform : FabricBaseInnerPlatform(), ModInnerPlatform {
    override val commonEnergy: EnergyUnit
        get() = Energies.TECH_REBORN_ENERGY
    override val modID: String
        get() = PeripheralWorksCore.MOD_ID
}
