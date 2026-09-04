package site.siredvin.peripheralworks.common.configuration.integration

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration

object AutomobilityConfiguration : IntegrationConfiguration {

    override val modID: String
        get() = "automobility"

    private var enableAutomobilityConfig: ForgeConfigSpec.BooleanValue? = null

    val enableAutomobile: Boolean
        get() = enableAutomobilityConfig?.get() ?: true

    override val name: String
        get() = "automobility"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableAutomobilityConfig = builder.comment("Enables automobile entity integration")
            .define("enableAutomobile", true)
    }
}
