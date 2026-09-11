package site.siredvin.peripheralworks.common.configuration.integration

import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration
import net.neoforged.neoforge.common.ModConfigSpec as ForgeConfigSpec

object FluxNetworksConfiguration : IntegrationConfiguration {

    override val modID: String
        get() = "fluxnetworks"

    private var enableFluxControllerConfig: ForgeConfigSpec.BooleanValue? = null

    val enableFluxController: Boolean
        get() = enableFluxControllerConfig.getOrDefault(true)

    override val name: String
        get() = "flux_networks"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableFluxControllerConfig = builder.comment("Enables flux controller integration")
            .define("enableFluxController", true)
    }
}
