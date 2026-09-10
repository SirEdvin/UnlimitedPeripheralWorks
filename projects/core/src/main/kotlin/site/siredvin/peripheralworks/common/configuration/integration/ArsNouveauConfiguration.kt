package site.siredvin.peripheralworks.common.configuration.integration

import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration
import net.neoforged.neoforge.common.ModConfigSpec as ForgeConfigSpec

object ArsNouveauConfiguration : IntegrationConfiguration {

    override val modID: String
        get() = "ars_nouveau"
    override val name: String
        get() = "ars_nouveau"

    private var enableCasterTomePocketUpgradeConfig: ForgeConfigSpec.BooleanValue? = null
    private var enableSourceStorageConfig: ForgeConfigSpec.BooleanValue? = null
    private var enableMobJarPluginConfig: ForgeConfigSpec.BooleanValue? = null

    val enableCasterTomePocketUpgrade: Boolean
        get() = enableCasterTomePocketUpgradeConfig.getOrDefault(true)

    val enableSourceStorage: Boolean
        get() = enableSourceStorageConfig.getOrDefault(true)

    val enableMobJarPlugin: Boolean
        get() = enableMobJarPluginConfig.getOrDefault(true)

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableCasterTomePocketUpgradeConfig = builder.comment("Enables usage of caster tome as pocket computer upgrade")
            .define("enableCasterTomePocketUpgrade", true)
        enableSourceStorageConfig = builder.comment("Enabled source storage")
            .define("enableSourceStorage", true)
        enableMobJarPluginConfig = builder.comment("Enable mob jar plugin")
            .define("enableMobJarPlugin", true)
    }
}
