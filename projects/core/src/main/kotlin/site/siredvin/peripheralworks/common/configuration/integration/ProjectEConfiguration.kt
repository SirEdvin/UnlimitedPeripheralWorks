package site.siredvin.peripheralworks.common.configuration.integration

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration

object ProjectEConfiguration : IntegrationConfiguration {

    override val modID: String
        get() = "projecte"

    private var enableTurtleUpgradeConfig: ForgeConfigSpec.BooleanValue? = null
    private var enablePocketUpgradeConfig: ForgeConfigSpec.BooleanValue? = null

    val enableTransmutationTabletTurtleUpgrade: Boolean
        get() = enableTurtleUpgradeConfig.getOrDefault(true)

    val enableTransmutationTabletPocketUpgrade: Boolean
        get() = enablePocketUpgradeConfig.getOrDefault(true)

    override val name: String
        get() = "projecte"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableTurtleUpgradeConfig = builder.comment("Enables usage of transmutation tablet as turtle upgrade")
            .define("enableTransmutationTabletTurtleUpgrade", true)
        enablePocketUpgradeConfig = builder.comment("Enables usage of transmutation tablet as pocket upgrade")
            .define("enableTransmutationTabletPocketUpgrade", true)
    }
}
