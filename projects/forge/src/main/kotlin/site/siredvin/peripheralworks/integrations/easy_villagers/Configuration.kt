package site.siredvin.peripheralworks.integrations.easy_villagers

import site.siredvin.peripheralworks.api.IForgeConfigHandler
import net.neoforged.neoforge.common.ModConfigSpec as ForgeConfigSpec

object Configuration : IForgeConfigHandler {

    private var enableAutoTradeConfig: ForgeConfigSpec.BooleanValue? = null

    val enableAutoTrader: Boolean
        get() = enableAutoTradeConfig?.get() ?: true

    override val name: String
        get() = "easy_villagers"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableAutoTradeConfig = builder.comment("Enables auto trader integration").define("enableAutoTrader", true)
    }
}
