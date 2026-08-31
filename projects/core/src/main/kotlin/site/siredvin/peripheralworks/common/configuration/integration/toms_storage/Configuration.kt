package site.siredvin.peripheralworks.common.configuration.integration.toms_storage

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {

    private var enableTomsStorageConfig: ForgeConfigSpec.BooleanValue? = null

    val enableTomsStorage: Boolean
        get() = enableTomsStorageConfig?.get() ?: true

    override val name: String
        get() = "toms_storage"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableTomsStorageConfig = builder.comment("Enables Tom's Storage integration; generic storage integrations remain available when disabled")
            .define("enableTomsStorage", true)
    }
}
