package site.siredvin.peripheralworks.integrations.team_reborn_energy

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.config.IConfigHandler

object Configuration : IConfigHandler {

    private const val defaultEnergyToFuelRate = 50 // really calculated :) Trust me

    private var ENABLE_ENERGY_STORAGE: ForgeConfigSpec.BooleanValue? = null
    private var ENABLE_TURTLE_REFUEL_WITH_ENERGY: ForgeConfigSpec.BooleanValue? = null
    private var ENERGY_TO_FUEL_RATE: ForgeConfigSpec.IntValue? = null

    val enableEnergyStorage: Boolean
        get() = ENABLE_ENERGY_STORAGE?.get() ?: true
    val enableTurtleRefulWithEnergy: Boolean
        get() = ENABLE_TURTLE_REFUEL_WITH_ENERGY?.get() ?: true
    val energyToFuelRate: Int
        get() = ENERGY_TO_FUEL_RATE?.get() ?: defaultEnergyToFuelRate

    override val name: String
        get() = "team_reborn_energy"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        ENABLE_ENERGY_STORAGE = builder.comment("Enables energy storage integration").define("enableEnergyStorage", true)
        ENABLE_TURTLE_REFUEL_WITH_ENERGY = builder.comment("Enables turtle refueling with items with energy")
            .define("enableTurtleRefuelWithEnergy", true)
        ENERGY_TO_FUEL_RATE = builder.comment("Controls how many energy required for one fuel point")
            .defineInRange("energyToFuelRate", defaultEnergyToFuelRate, 1, Int.MAX_VALUE)
    }
}
