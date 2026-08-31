package site.siredvin.peripheralworks.common.configuration.integration.additionallanterns

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {

    private var enableLanternsConfig: ForgeConfigSpec.BooleanValue? = null

    val enableLanterns: Boolean
        get() = enableLanternsConfig?.get() ?: true

    override val name: String
        get() = "additionallanterns"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableLanternsConfig = builder.comment("Enables lanterns integration").define("enableLanterns", true)
    }
}
