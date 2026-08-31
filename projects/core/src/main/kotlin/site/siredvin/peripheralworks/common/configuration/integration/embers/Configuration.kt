package site.siredvin.peripheralworks.common.configuration.integration.embers

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {
    override val name: String
        get() = "embers"

    private var enableEmberStorageConfig: ForgeConfigSpec.BooleanValue? = null

    val enableEmberStorage: Boolean
        get() = enableEmberStorageConfig?.get() != false

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableEmberStorageConfig = builder.comment("Enables ember storage integration")
            .define("enableEmberStorage", true)
    }
}
