package site.siredvin.peripheralworks.common.configuration.integration

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration

object TheurgyConfiguration : IntegrationConfiguration {

    override val modID: String
        get() = "theurgy"

    override val name: String
        get() = "theurgy"

    private var enableMercuryFluxStorageConfig: ForgeConfigSpec.BooleanValue? = null

    val enableMercuryFluxStorage: Boolean
        get() = enableMercuryFluxStorageConfig.getOrDefault(true)

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableMercuryFluxStorageConfig = builder.comment("Enables mercury flux storage integration")
            .define("enableMercuryFluxStorage", true)
    }
}
