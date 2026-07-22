package site.siredvin.peripheralworks.integrations.embers

import site.siredvin.peripheralworks.api.IForgeConfigHandler
import net.neoforged.neoforge.common.ModConfigSpec as ForgeConfigSpec

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
