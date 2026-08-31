package site.siredvin.peripheralworks.common.configuration.integration.alloy_forgery

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {

    private var enableAlloyForgeryConfig: ForgeConfigSpec.BooleanValue? = null

    val enableAlloyForgery: Boolean
        get() = enableAlloyForgeryConfig?.get() ?: true

    override val name: String
        get() = "alloy_forgery"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableAlloyForgeryConfig = builder.comment("Enables alloy forgery integration")
            .define("enableAlloyForgery", true)
    }
}
