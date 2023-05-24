package site.siredvin.peripheralworks.integrations.create

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.IConfigHandler

object Configuration: IConfigHandler {

    private var ENABLE_STORAGE: ForgeConfigSpec.BooleanValue? = null

    val enableStorage: Boolean
        get() = ENABLE_STORAGE?.get() ?: true

    override val name: String
        get() = "create"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        ENABLE_STORAGE = builder.comment("Enables storage integration").define("enableStorage", true)
    }
}