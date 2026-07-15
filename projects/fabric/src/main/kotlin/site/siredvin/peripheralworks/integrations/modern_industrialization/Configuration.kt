package site.siredvin.peripheralworks.integrations.modern_industrialization

import net.neoforged.neoforge.common.ModConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {

    private var enableEnergyStorageConfig: ModConfigSpec.BooleanValue? = null
    private var enableCraftingMachineConfig: ModConfigSpec.BooleanValue? = null

    val enableEnergyStorage: Boolean
        get() = enableEnergyStorageConfig?.get() ?: true
    val enableCraftingMachine: Boolean
        get() = enableCraftingMachineConfig?.get() ?: true

    override val name: String
        get() = "modern_industrialization"

    override fun addToConfig(builder: ModConfigSpec.Builder) {
        enableEnergyStorageConfig = builder.comment("Enables energy storage integration").define("enableEnergyStorage", true)
        enableCraftingMachineConfig = builder.comment("Enables crafting machine integration").define("enableCraftingMachine", true)
    }
}
