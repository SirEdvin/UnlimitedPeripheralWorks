package site.siredvin.peripheralworks.integrations.fluxnetworks

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.config.IConfigHandler

object Configuration : IConfigHandler {

    private var ENABLE_FLUX_CONTROLLER: ForgeConfigSpec.BooleanValue? = null

    val enableFluxController: Boolean
        get() = ENABLE_FLUX_CONTROLLER?.get() ?: true

    override val name: String
        get() = "flux_networks"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        ENABLE_FLUX_CONTROLLER = builder.comment("Enables flux controller integration")
            .define("enableAutomobile", true)
    }
}
