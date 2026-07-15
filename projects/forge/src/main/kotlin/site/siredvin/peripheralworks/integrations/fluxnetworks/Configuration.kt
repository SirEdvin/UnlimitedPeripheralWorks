package site.siredvin.peripheralworks.integrations.fluxnetworks

import site.siredvin.peripheralworks.api.IForgeConfigHandler
import net.neoforged.neoforge.common.ModConfigSpec as ForgeConfigSpec

object Configuration : IForgeConfigHandler {

    private var enableFluxControllerConfig: ForgeConfigSpec.BooleanValue? = null

    val enableFluxController: Boolean
        get() = enableFluxControllerConfig?.get() != false

    override val name: String
        get() = "flux_networks"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableFluxControllerConfig = builder.comment("Enables flux controller integration")
            .define("enableFluxController", true)
    }
}
