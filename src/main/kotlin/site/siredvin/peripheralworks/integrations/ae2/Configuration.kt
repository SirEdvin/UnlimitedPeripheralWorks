package site.siredvin.peripheralworks.integrations.ae2

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.IConfigHandler

object Configuration : IConfigHandler {

    private var ENABLE_ME_INTERFACE: ForgeConfigSpec.BooleanValue? = null

    val enableMEInterface: Boolean
        get() = ENABLE_ME_INTERFACE?.get() ?: true

    override val name: String
        get() = "ae2"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        ENABLE_ME_INTERFACE = builder.comment("Enables me interface integration").define("enableMEInterface", true)
    }
}
