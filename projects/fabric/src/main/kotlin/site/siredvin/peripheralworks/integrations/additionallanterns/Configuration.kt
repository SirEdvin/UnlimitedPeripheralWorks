package site.siredvin.peripheralworks.integrations.additionallanterns

import net.neoforged.neoforge.common.ModConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {

    private var enableLanternsConfig: ModConfigSpec.BooleanValue? = null

    val enableLanterns: Boolean
        get() = enableLanternsConfig?.get() ?: true

    override val name: String
        get() = "additionallanterns"

    override fun addToConfig(builder: ModConfigSpec.Builder) {
        enableLanternsConfig = builder.comment("Enables lanterns integration").define("enableLanterns", true)
    }
}
