package site.siredvin.peripheralworks.common.configuration.integration

import net.neoforged.neoforge.common.ModConfigSpec
import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration

object AdditionalLanternsConfiguration : IntegrationConfiguration {

    override val modID: String
        get() = "additionallanterns"

    private var enableLanternsConfig: ModConfigSpec.BooleanValue? = null

    val enableLanterns: Boolean
        get() = enableLanternsConfig.getOrDefault(true)

    override val name: String
        get() = "additionallanterns"

    override fun addToConfig(builder: ModConfigSpec.Builder) {
        enableLanternsConfig = builder.comment("Enables lanterns integration").define("enableLanterns", true)
    }
}
