package site.siredvin.peripheralworks.common.configuration.integration

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration

object ModernIndustrializationConfiguration : IntegrationConfiguration {

    override val modID: String
        get() = "modern_industrialization"

    private var enableEnergyStorageConfig: ForgeConfigSpec.BooleanValue? = null
    private var enableCraftingMachineConfig: ForgeConfigSpec.BooleanValue? = null

    val enableEnergyStorage: Boolean
        get() = enableEnergyStorageConfig?.get() ?: true
    val enableCraftingMachine: Boolean
        get() = enableCraftingMachineConfig?.get() ?: true

    override val name: String
        get() = "modern_industrialization"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableEnergyStorageConfig = builder.comment("Enables energy storage integration").define("enableEnergyStorage", true)
        enableCraftingMachineConfig = builder.comment("Enables crafting machine integration").define("enableCraftingMachine", true)
    }
}
