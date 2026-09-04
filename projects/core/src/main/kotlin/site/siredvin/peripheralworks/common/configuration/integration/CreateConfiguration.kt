package site.siredvin.peripheralworks.common.configuration.integration

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration

object CreateConfiguration : IntegrationConfiguration {

    override val modID: String
        get() = "create"

    private var enableCreateIntegrationConfig: ForgeConfigSpec.BooleanValue? = null

    val enableCreateIntegration: Boolean
        get() = enableCreateIntegrationConfig.getOrDefault(true)

    override val name: String
        get() = "create"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableCreateIntegrationConfig = builder.comment("Enables create integration")
            .define("enableCreateIntegration", true)
    }
}
