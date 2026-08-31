package site.siredvin.peripheralworks.common.configuration.integration.theurgy

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {

    override val name: String
        get() = "theurgy"

    private var enableMercuryFluxStorageConfig: ForgeConfigSpec.BooleanValue? = null

    val enableMercuryFluxStorage: Boolean
        get() = enableMercuryFluxStorageConfig?.get() != false

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableMercuryFluxStorageConfig = builder.comment("Enables mercury flux storage integration")
            .define("enableMercuryFluxStorage", true)
    }
}
