package site.siredvin.peripheralworks.integrations.team_reborn_energy

import net.neoforged.neoforge.common.ModConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {

    private const val DEFAULT_ENERGY_TO_FUEL_RATE = 50

    private var enableEnergyStorageConfig: ModConfigSpec.BooleanValue? = null
    private var enableTurtleRefuelWithEnergyConfig: ModConfigSpec.BooleanValue? = null
    private var energyToFuelRateConfig: ModConfigSpec.IntValue? = null

    val enableEnergyStorage: Boolean
        get() = enableEnergyStorageConfig?.get() ?: true
    val enableTurtleRefuelWithEnergy: Boolean
        get() = enableTurtleRefuelWithEnergyConfig?.get() ?: true
    val energyToFuelRate: Int
        get() = energyToFuelRateConfig?.get() ?: DEFAULT_ENERGY_TO_FUEL_RATE

    override val name: String
        get() = "team_reborn_energy"

    override fun addToConfig(builder: ModConfigSpec.Builder) {
        enableEnergyStorageConfig = builder.comment("Enables energy storage integration").define("enableEnergyStorage", true)
        enableTurtleRefuelWithEnergyConfig = builder.comment("Enables turtle refueling with items with energy")
            .define("enableTurtleRefuelWithEnergy", true)
        energyToFuelRateConfig = builder.comment("Controls how many energy required for one fuel point")
            .defineInRange("energyToFuelRate", DEFAULT_ENERGY_TO_FUEL_RATE, 1, Int.MAX_VALUE)
    }
}
