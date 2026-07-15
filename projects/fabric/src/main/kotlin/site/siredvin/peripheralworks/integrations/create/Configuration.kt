package site.siredvin.peripheralworks.integrations.create

import net.neoforged.neoforge.common.ModConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {

    private var enableCreateIntegrationConfig: ModConfigSpec.BooleanValue? = null

    val enableCreateIntegration: Boolean
        get() = enableCreateIntegrationConfig?.get() ?: true

    override val name: String
        get() = "create"

    override fun addToConfig(builder: ModConfigSpec.Builder) {
        enableCreateIntegrationConfig = builder.comment("Enables create integration")
            .define("enableCreateIntegration", true)
    }
}
