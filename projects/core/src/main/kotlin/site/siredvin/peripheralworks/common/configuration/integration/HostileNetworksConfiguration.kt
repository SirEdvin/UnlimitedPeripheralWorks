package site.siredvin.peripheralworks.common.configuration.integration

import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration
import net.neoforged.neoforge.common.ModConfigSpec as ForgeConfigSpec

object HostileNetworksConfiguration : IntegrationConfiguration {
    override val modID: String = "hostilenetworks"
    override val name: String = "hostilenetworks"
    private var enabledConfig: ForgeConfigSpec.BooleanValue? = null
    val enabled: Boolean
        get() = enabledConfig.getOrDefault(true)

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enabledConfig = builder.comment("Enables loot selection and model item details").define("enabled", true)
    }
}
