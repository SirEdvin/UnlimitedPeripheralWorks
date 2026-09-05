package site.siredvin.peripheralworks.common.configuration.integration

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration

object UniversalShopsConfiguration : IntegrationConfiguration {

    override val modID: String
        get() = "universal_shops"

    private var enableShopsConfig: ForgeConfigSpec.BooleanValue? = null

    val enableShops: Boolean
        get() = enableShopsConfig.getOrDefault(true)

    override val name: String
        get() = "universal_shops"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableShopsConfig = builder.comment("Enables shops integration")
            .define("enableShops", true)
    }
}
