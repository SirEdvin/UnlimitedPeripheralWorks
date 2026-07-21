package site.siredvin.peripheralworks.integrations.toms_storage

import site.siredvin.peripheralworks.api.IForgeConfigHandler
import net.neoforged.neoforge.common.ModConfigSpec as ForgeConfigSpec

object Configuration : IForgeConfigHandler {

    private var enableTomsStorageConfig: ForgeConfigSpec.BooleanValue? = null

    val enableTomsStorage: Boolean
        get() = enableTomsStorageConfig?.get() ?: true

    override val name: String
        get() = "toms_storage"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableTomsStorageConfig = builder.comment("Enables toms storage integration, even if you disable this, generic inventory integration will work")
            .define("enableTomsStorage", true)
    }
}
