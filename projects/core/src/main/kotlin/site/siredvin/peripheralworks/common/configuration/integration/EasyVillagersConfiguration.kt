package site.siredvin.peripheralworks.common.configuration.integration

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration

object EasyVillagersConfiguration : IntegrationConfiguration {

    override val modID: String
        get() = "easy_villagers"

    private var enableAutoTradeConfig: ForgeConfigSpec.BooleanValue? = null

    val enableAutoTrader: Boolean
        get() = enableAutoTradeConfig.getOrDefault(true)

    override val name: String
        get() = "easy_villagers"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableAutoTradeConfig = builder.comment("Enables auto trader integration").define("enableAutoTrader", true)
    }
}
