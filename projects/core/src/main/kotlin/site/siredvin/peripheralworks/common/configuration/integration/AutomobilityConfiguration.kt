package site.siredvin.peripheralworks.common.configuration.integration

import net.neoforged.neoforge.common.ModConfigSpec
import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration

object AutomobilityConfiguration : IntegrationConfiguration {

    override val modID: String
        get() = "automobility"

    private var enableAutomobilityConfig: ModConfigSpec.BooleanValue? = null

    val enableAutomobile: Boolean
        get() = enableAutomobilityConfig.getOrDefault(true)

    override val name: String
        get() = "automobility"

    override fun addToConfig(builder: ModConfigSpec.Builder) {
        enableAutomobilityConfig = builder.comment("Enables automobile entity integration")
            .define("enableAutomobile", true)
    }
}
