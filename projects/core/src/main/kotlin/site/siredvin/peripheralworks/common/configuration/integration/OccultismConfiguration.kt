package site.siredvin.peripheralworks.common.configuration.integration

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration

object OccultismConfiguration : IntegrationConfiguration {

    override val modID: String
        get() = "occultism"

    private var enableOccultismStorageConfig: ForgeConfigSpec.BooleanValue? = null
    private var enableOccultismGoldenBowlConfig: ForgeConfigSpec.BooleanValue? = null

    val enableOccultismStorage: Boolean
        get() = enableOccultismStorageConfig.getOrDefault(true)

    val enableOccultismGoldenBowl: Boolean
        get() = enableOccultismGoldenBowlConfig.getOrDefault(true)

    override val name: String
        get() = "occultism"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableOccultismStorageConfig = builder.comment("Enables occultism storage integration").define("enableOccultismStorage", true)
        enableOccultismGoldenBowlConfig = builder.comment("Enables occultism golden bowl integration").define("enableOccultismGoldenBowl", true)
    }
}
