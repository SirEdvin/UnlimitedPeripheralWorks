package site.siredvin.peripheralworks.common.configuration.integration.deepresonance

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {

    private var enableResonatingCrystalConfig: ForgeConfigSpec.BooleanValue? = null
    private var enableGeneratorPartConfig: ForgeConfigSpec.BooleanValue? = null
    private var enableTankConfig: ForgeConfigSpec.BooleanValue? = null

    val enableResonatingCrystal: Boolean
        get() = enableResonatingCrystalConfig?.get() ?: true

    val enableGeneratorPart: Boolean
        get() = enableGeneratorPartConfig?.get() ?: true

    val enableTank: Boolean
        get() = enableTankConfig?.get() ?: true

    override val name: String
        get() = "deep_resonance"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableResonatingCrystalConfig = builder.comment("Enables resonating crystal integration").define("enableResonatingCrystal", true)
        enableGeneratorPartConfig = builder.comment("Enables generator part integration").define("enableGeneratorPart", true)
        enableTankConfig = builder.comment("Enables tank").define("enableTank", true)
    }
}
