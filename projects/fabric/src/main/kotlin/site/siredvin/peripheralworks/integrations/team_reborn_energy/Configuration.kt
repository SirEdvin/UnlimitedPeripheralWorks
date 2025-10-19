package site.siredvin.peripheralworks.integrations.team_reborn_energy

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {

    private const val DEFAULT_ENERGY_TO_FUEL_RATE = 256 // really calculated :) Trust me

    private var enableEnergyStorageConfig: ForgeConfigSpec.BooleanValue? = null

    val enableEnergyStorage: Boolean
        get() = enableEnergyStorageConfig?.get() ?: true

    override val name: String
        get() = "team_reborn_energy"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableEnergyStorageConfig = builder.comment("Enables energy storage integration").define("enableEnergyStorage", true)
    }
}
