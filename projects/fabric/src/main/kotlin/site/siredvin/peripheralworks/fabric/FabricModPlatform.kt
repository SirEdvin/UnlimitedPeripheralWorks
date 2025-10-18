package site.siredvin.peripheralworks.fabric

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModEnvironment
import site.siredvin.broccolium.modules.storage.energy.Energies
import site.siredvin.broccolium.modules.storage.energy.EnergyUnit
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.xplat.ModInnerPlatform
import site.siredvin.tweakium.modules.platform.FabricInnerComputerBasePlatform

object FabricModPlatform : FabricInnerComputerBasePlatform(), ModInnerPlatform {
    override val commonEnergy: EnergyUnit
        get() = Energies.REDSTONE_FLUX
    override val modList: List<String>
        get() = FabricLoader.getInstance().allMods.filter { it.metadata.environment != ModEnvironment.SERVER }.map { it.metadata.name }

    override fun getModInformation(mod: String): Map<String, Any>? {
        val mod = FabricLoader.getInstance().allMods.firstOrNull { it.metadata.name == mod && it.metadata.environment != ModEnvironment.SERVER } ?: return null
        return mapOf(
            "name" to mod.metadata.name,
            "version" to mod.metadata.version,
            "description" to mod.metadata.description,
            "license" to mod.metadata.license,
        )
    }

    override val modID: String
        get() = PeripheralWorksCore.MOD_ID
}
