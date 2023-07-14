package site.siredvin.peripheralworks.integrations.universal_shops

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.config.IConfigHandler

object Configuration : IConfigHandler {

    private var ENABLE_SHOPS: ForgeConfigSpec.BooleanValue? = null

    val enableShops: Boolean
        get() = ENABLE_SHOPS?.get() ?: true

    override val name: String
        get() = "universal_shops"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        ENABLE_SHOPS = builder.comment("Enables shops integration")
            .define("enableShops", true)
    }
}
