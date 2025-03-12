package site.siredvin.peripheralworks.integrations.ae2

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

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
        enableMeInterfaceConfig = builder.comment("Enables me integration with storages").define("enableStorageIntegrations", true)
    }
}
