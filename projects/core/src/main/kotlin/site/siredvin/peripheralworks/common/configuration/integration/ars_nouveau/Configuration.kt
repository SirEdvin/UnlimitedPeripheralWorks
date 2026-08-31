package site.siredvin.peripheralworks.common.configuration.integration.ars_nouveau

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {
    override val name: String
        get() = "ars_nouveau"

    private var enableCasterTomePocketUpgradeConfig: ForgeConfigSpec.BooleanValue? = null
    private var enableSourceStorageConfig: ForgeConfigSpec.BooleanValue? = null
    private var enableMobJarPluginConfig: ForgeConfigSpec.BooleanValue? = null

    val enableCasterTomePocketUpgrade: Boolean
        get() = enableCasterTomePocketUpgradeConfig?.get() != false

    val enableSourceStorage: Boolean
        get() = enableSourceStorageConfig?.get() != false

    val enableMobJarPlugin: Boolean
        get() = enableMobJarPluginConfig?.get() != false

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableCasterTomePocketUpgradeConfig = builder.comment("Enables usage of caster tome as pocket computer upgrade")
            .define("enableCasterTomePocketUpgrade", true)
        enableSourceStorageConfig = builder.comment("Enabled source storage")
            .define("enableSourceStorage", true)
        enableMobJarPluginConfig = builder.comment("Enable mob jar plugin")
            .define("enableMobJarPlugin", true)
    }
}
