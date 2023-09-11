package site.siredvin.peripheralworks.integrations.modern_industrialization

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.config.IConfigHandler

object Configuration : IConfigHandler {

    private var ENABLE_ENERGY_STORAGE: ForgeConfigSpec.BooleanValue? = null
    private var ENABLE_CRAFTING_MACHINE: ForgeConfigSpec.BooleanValue? = null

    val enableEnergyStorage: Boolean
        get() = ENABLE_ENERGY_STORAGE?.get() ?: true
    val enableCraftingMachine: Boolean
        get() = ENABLE_CRAFTING_MACHINE?.get() ?: true

    override val name: String
        get() = "modern_industrialization"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        ENABLE_ENERGY_STORAGE = builder.comment("Enables energy storage integration").define("enableEnergyStorage", true)
        ENABLE_CRAFTING_MACHINE = builder.comment("Enables crafting machine integration").define("enableCraftingMachine", true)
    }
}
