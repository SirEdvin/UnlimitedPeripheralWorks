package site.siredvin.peripheralworks.integrations.create

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

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
