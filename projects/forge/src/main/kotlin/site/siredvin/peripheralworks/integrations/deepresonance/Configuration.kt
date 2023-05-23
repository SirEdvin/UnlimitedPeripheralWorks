package site.siredvin.peripheralworks.integrations.deepresonance

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.config.IConfigHandler

object Configuration: IConfigHandler {

    private var ENABLE_RESONATING_CRYSTAL: ForgeConfigSpec.BooleanValue? = null
    private var ENABLE_GENERATOR_PART: ForgeConfigSpec.BooleanValue? = null
    private var ENABLE_TANK: ForgeConfigSpec.BooleanValue? = null

    val enableResonatingCrystal: Boolean
        get() = ENABLE_RESONATING_CRYSTAL?.get() ?: true

    val enableGeneratorPart: Boolean
        get() = ENABLE_GENERATOR_PART?.get() ?: true

    val enableTank: Boolean
        get() = ENABLE_TANK?.get() ?: true

    override val name: String
        get() = "deep_resonance"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        ENABLE_RESONATING_CRYSTAL = builder.comment("Enables resonating crystal integration").define("enableResonatingCrystal", true)
        ENABLE_GENERATOR_PART = builder.comment("Enables generator part integration").define("enableGeneratorPart", true)
        ENABLE_TANK = builder.comment("Enables tank").define("enableTank", true)
    }
}