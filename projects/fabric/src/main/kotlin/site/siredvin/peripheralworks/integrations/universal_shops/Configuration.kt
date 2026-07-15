package site.siredvin.peripheralworks.integrations.universal_shops

import net.neoforged.neoforge.common.ModConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {

    private var enableShopsConfig: ModConfigSpec.BooleanValue? = null

    val enableShops: Boolean
        get() = enableShopsConfig?.get() ?: true

    override val name: String
        get() = "universal_shops"

    override fun addToConfig(builder: ModConfigSpec.Builder) {
        enableShopsConfig = builder.comment("Enables shops integration")
            .define("enableShops", true)
    }
}
