@file:Suppress("ktlint:standard:package-name")

package site.siredvin.peripheralworks.integrations.toms_storage

import net.neoforged.neoforge.common.ModConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {

    private var enableTomsStorageConfig: ModConfigSpec.BooleanValue? = null

    val enableTomsStorage: Boolean
        get() = enableTomsStorageConfig?.get() ?: true

    override val name: String
        get() = "toms_storage"

    override fun addToConfig(builder: ModConfigSpec.Builder) {
        enableTomsStorageConfig = builder.comment("Enables toms storage integration, even if you disable this, generic item storage integration will work time-to-time")
            .define("enableTomsStorage", true)
    }
}
