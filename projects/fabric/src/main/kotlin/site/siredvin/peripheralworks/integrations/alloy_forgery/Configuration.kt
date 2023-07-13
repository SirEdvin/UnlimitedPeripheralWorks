package site.siredvin.peripheralworks.integrations.alloy_forgery

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.config.IConfigHandler

object Configuration : IConfigHandler {

    private var ENABLE_ALLOY_FORGERY: ForgeConfigSpec.BooleanValue? = null

    val enableAlloyForgery: Boolean
        get() = ENABLE_ALLOY_FORGERY?.get() ?: true

    override val name: String
        get() = "alloy_forgery"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        ENABLE_ALLOY_FORGERY = builder.comment("Enables alloy forgery integration")
            .define("enableAlloyForgery", true)
    }
}
