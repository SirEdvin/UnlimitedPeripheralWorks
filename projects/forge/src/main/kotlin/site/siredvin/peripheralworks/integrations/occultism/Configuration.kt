package site.siredvin.peripheralworks.integrations.occultism

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.IConfigHandler

object Configuration: IConfigHandler {

    private var ENABLE_OCCULTISM_STORAGE: ForgeConfigSpec.BooleanValue? = null
    private var ENABLE_OCCULTISM_GOLDEN_BOWL: ForgeConfigSpec.BooleanValue? = null

    val enableOccultismStorage: Boolean
        get() = ENABLE_OCCULTISM_STORAGE?.get() ?: true

    val enableOccultismGoldenBowl: Boolean
        get() = ENABLE_OCCULTISM_GOLDEN_BOWL?.get() ?: true

    override val name: String
        get() = "occultism"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        ENABLE_OCCULTISM_STORAGE = builder.comment("Enables occultism storage integration").define("enableOccultismStorage", true)
        ENABLE_OCCULTISM_STORAGE = builder.comment("Enables occultism golden bowl integration").define("enableOccultismGoldenBowl", true)
    }
}