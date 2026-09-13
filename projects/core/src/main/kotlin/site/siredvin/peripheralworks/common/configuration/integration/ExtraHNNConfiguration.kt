package site.siredvin.peripheralworks.common.configuration.integration

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration

object ExtraHNNConfiguration : IntegrationConfiguration {
    override val modID: String = "extrahnn"
    override val name: String = "extrahnn"
    private var enabledConfig: ForgeConfigSpec.BooleanValue? = null
    val enabled: Boolean
        get() = enabledConfig.getOrDefault(true)

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enabledConfig = builder.comment("Enables loot selection and model item details").define("enabled", true)
    }
}
