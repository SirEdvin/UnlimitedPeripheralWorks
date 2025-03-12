package site.siredvin.peripheralworks.integrations.occultism

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {

    private var enableOccultismStorageConfig: ForgeConfigSpec.BooleanValue? = null
    private var enableOccultismGoldenBowlConfig: ForgeConfigSpec.BooleanValue? = null

    val enableOccultismStorage: Boolean
        get() = enableOccultismStorageConfig?.get() ?: true

    val enableOccultismGoldenBowl: Boolean
        get() = enableOccultismGoldenBowlConfig?.get() ?: true

    override val name: String
        get() = "occultism"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableOccultismStorageConfig = builder.comment("Enables occultism storage integration").define("enableOccultismStorage", true)
        enableOccultismStorageConfig = builder.comment("Enables occultism golden bowl integration").define("enableOccultismGoldenBowl", true)
    }
}
