package site.siredvin.peripheralworks.common.configuration.integration

import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration
import net.neoforged.neoforge.common.ModConfigSpec as ForgeConfigSpec

object TomsStorageConfiguration : IntegrationConfiguration {

    override val modID: String
        get() = "toms_storage"

    private var enableTomsStorageConfig: ForgeConfigSpec.BooleanValue? = null

    val enableTomsStorage: Boolean
        get() = enableTomsStorageConfig.getOrDefault(true)

    override val name: String
        get() = "toms_storage"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableTomsStorageConfig = builder.comment("Enables Tom's Storage integration; generic storage integrations remain available when disabled")
            .define("enableTomsStorage", true)
    }
}
