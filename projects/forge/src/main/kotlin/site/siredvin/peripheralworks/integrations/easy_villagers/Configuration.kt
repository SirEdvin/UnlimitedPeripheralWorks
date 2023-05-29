package site.siredvin.peripheralworks.integrations.easy_villagers

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.config.IConfigHandler

object Configuration : IConfigHandler {

    private var ENABLE_AUTO_TRADER: ForgeConfigSpec.BooleanValue? = null

    val enableAutoTrader: Boolean
        get() = ENABLE_AUTO_TRADER?.get() ?: true

    override val name: String
        get() = "easy_villagers"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        ENABLE_AUTO_TRADER = builder.comment("Enables auto trader integration").define("enableAutoTrader", true)
    }
}
