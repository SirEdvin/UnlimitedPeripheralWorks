package site.siredvin.peripheralworks.integrations.ae2

import net.neoforged.neoforge.common.ModConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {

    private var enableMeInterfaceConfig: ModConfigSpec.BooleanValue? = null
    private var enableStorageIntegrationConfig: ModConfigSpec.BooleanValue? = null

    val enableMEInterface: Boolean
        get() = enableMeInterfaceConfig?.get() ?: true

    val enableStorageIntegrations: Boolean
        get() = enableStorageIntegrationConfig?.get() ?: true

    override val name: String
        get() = "ae2"

    override fun addToConfig(builder: ModConfigSpec.Builder) {
        enableMeInterfaceConfig = builder.comment("Enables me blocks integration").define("enableMEInterface", true)
        enableStorageIntegrationConfig = builder.comment("Enables me integration with storages").define("enableStorageIntegrations", true)
    }
}
