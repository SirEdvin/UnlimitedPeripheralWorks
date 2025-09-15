package site.siredvin.peripheralworks.integrations.fluxnetworks

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

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
