package site.siredvin.peripheralworks.integrations.create

import site.siredvin.peripheralworks.api.IForgeConfigHandler
import net.neoforged.neoforge.common.ModConfigSpec as ForgeConfigSpec

object Configuration : IForgeConfigHandler {

    private var enableCreateIntegrationConfig: ForgeConfigSpec.BooleanValue? = null

    val enableCreateIntegration: Boolean
        get() = enableCreateIntegrationConfig?.get() ?: true

    override val name: String
        get() = "create"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableCreateIntegrationConfig = builder.comment("Enables create integration")
            .define("enableCreateIntegration", true)
    }
}
