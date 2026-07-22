package site.siredvin.peripheralworks.integrations.ae2

import site.siredvin.peripheralworks.api.IForgeConfigHandler
import net.neoforged.neoforge.common.ModConfigSpec as ForgeConfigSpec

object Configuration : IForgeConfigHandler {

    private var enableMeInterfaceConfig: ForgeConfigSpec.BooleanValue? = null
    private var enableStorageIntegrationConfig: ForgeConfigSpec.BooleanValue? = null

    val enableMEInterface: Boolean
        get() = enableMeInterfaceConfig?.get() ?: true

    val enableStorageIntegrations: Boolean
        get() = enableStorageIntegrationConfig?.get() ?: true

    override val name: String
        get() = "ae2"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableMeInterfaceConfig = builder.comment("Enables me blocks integration").define("enableMEInterface", true)
        enableStorageIntegrationConfig = builder.comment("Enables me integration with storages").define("enableStorageIntegrations", true)
    }
}
