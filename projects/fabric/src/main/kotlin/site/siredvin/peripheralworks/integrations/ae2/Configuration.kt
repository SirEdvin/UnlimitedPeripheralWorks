package site.siredvin.peripheralworks.integrations.ae2

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.config.IConfigHandler

object Configuration : IConfigHandler {

    private var ENABLE_ME_INTERFACE: ForgeConfigSpec.BooleanValue? = null
    private var ENABLE_STORAGE_INTEGRATIONDS: ForgeConfigSpec.BooleanValue? = null

    val enableMEInterface: Boolean
        get() = ENABLE_ME_INTERFACE?.get() ?: true

    val enableStorageIntegrations: Boolean
        get() = ENABLE_STORAGE_INTEGRATIONDS?.get() ?: true

    override val name: String
        get() = "ae2"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        ENABLE_ME_INTERFACE = builder.comment("Enables me blocks integration").define("enableMEInterface", true)
        ENABLE_ME_INTERFACE = builder.comment("Enables me integration with storages").define("enableStorageIntegrations", true)
    }
}
