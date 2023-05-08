package site.siredvin.peripheralworks.integrations.toms_storage

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.IConfigHandler

object Configuration: IConfigHandler {

    private var ENABLE_TOMS_STORAGE: ForgeConfigSpec.BooleanValue? = null

    val enableTomsStorage: Boolean
        get() = ENABLE_TOMS_STORAGE?.get() ?: true


    override val name: String
        get() = "toms_storage"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        ENABLE_TOMS_STORAGE = builder.comment("Enables toms storage integration, even if you disable this, generic item storage integration will work time-to-time")
            .define("enableTomsStorage", true)
    }
}