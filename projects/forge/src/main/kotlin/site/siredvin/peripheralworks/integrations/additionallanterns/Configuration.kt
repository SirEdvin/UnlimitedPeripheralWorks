package site.siredvin.peripheralworks.integrations.additionallanterns

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.config.IConfigHandler

object Configuration : IConfigHandler {

    private var ENABLE_LANTERNS: ForgeConfigSpec.BooleanValue? = null

    val enableLanterns: Boolean
        get() = ENABLE_LANTERNS?.get() ?: true

    override val name: String
        get() = "additionallanterns"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        ENABLE_LANTERNS = builder.comment("Enables lanterns integration").define("enableLanterns", true)
    }
}
