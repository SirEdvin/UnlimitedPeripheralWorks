package site.siredvin.peripheralworks.common.configuration.integration

import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration
import net.neoforged.neoforge.common.ModConfigSpec as ForgeConfigSpec

object AlloyForgeryConfiguration : IntegrationConfiguration {

    override val modID: String
        get() = "alloy_forgery"

    private var enableAlloyForgeryConfig: ForgeConfigSpec.BooleanValue? = null

    val enableAlloyForgery: Boolean
        get() = enableAlloyForgeryConfig.getOrDefault(true)

    override val name: String
        get() = "alloy_forgery"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableAlloyForgeryConfig = builder.comment("Enables alloy forgery integration")
            .define("enableAlloyForgery", true)
    }
}
