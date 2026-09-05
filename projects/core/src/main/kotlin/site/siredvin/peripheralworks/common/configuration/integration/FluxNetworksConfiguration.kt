package site.siredvin.peripheralworks.common.configuration.integration

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration

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
