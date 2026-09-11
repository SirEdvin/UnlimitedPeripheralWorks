package site.siredvin.peripheralworks.common.configuration.integration

import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration
import net.neoforged.neoforge.common.ModConfigSpec as ForgeConfigSpec

object EmbersConfiguration : IntegrationConfiguration {

    override val modID: String
        get() = "embers"
    override val name: String
        get() = "embers"

    private var enableEmberStorageConfig: ForgeConfigSpec.BooleanValue? = null

    val enableEmberStorage: Boolean
        get() = enableEmberStorageConfig.getOrDefault(true)

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableEmberStorageConfig = builder.comment("Enables ember storage integration")
            .define("enableEmberStorage", true)
    }
}
