package site.siredvin.peripheralworks.common.configuration.integration

import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration
import net.neoforged.neoforge.common.ModConfigSpec as ForgeConfigSpec

object ModernIndustrializationConfiguration : IntegrationConfiguration {

    override val modID: String
        get() = "modern_industrialization"

    private var enableEnergyStorageConfig: ForgeConfigSpec.BooleanValue? = null
    private var enableCraftingMachineConfig: ForgeConfigSpec.BooleanValue? = null

    val enableEnergyStorage: Boolean
        get() = enableEnergyStorageConfig.getOrDefault(true)
    val enableCraftingMachine: Boolean
        get() = enableCraftingMachineConfig.getOrDefault(true)

    override val name: String
        get() = "modern_industrialization"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableEnergyStorageConfig = builder.comment("Enables energy storage integration").define("enableEnergyStorage", true)
        enableCraftingMachineConfig = builder.comment("Enables crafting machine integration").define("enableCraftingMachine", true)
    }
}
