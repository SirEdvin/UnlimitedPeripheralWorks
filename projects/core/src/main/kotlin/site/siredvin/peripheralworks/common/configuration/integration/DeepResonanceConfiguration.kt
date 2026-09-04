package site.siredvin.peripheralworks.common.configuration.integration

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration

object DeepResonanceConfiguration : IntegrationConfiguration {

    override val modID: String
        get() = "deepresonance"

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
