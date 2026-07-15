package site.siredvin.peripheralworks.integrations.alloy_forgery

import net.neoforged.neoforge.common.ModConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {

    private var enableAlloyForgeryConfig: ModConfigSpec.BooleanValue? = null

    val enableAlloyForgery: Boolean
        get() = enableAlloyForgeryConfig?.get() ?: true

    override val name: String
        get() = "alloy_forgery"

    override fun addToConfig(builder: ModConfigSpec.Builder) {
        enableAlloyForgeryConfig = builder.comment("Enables alloy forgery integration")
            .define("enableAlloyForgery", true)
    }
}
