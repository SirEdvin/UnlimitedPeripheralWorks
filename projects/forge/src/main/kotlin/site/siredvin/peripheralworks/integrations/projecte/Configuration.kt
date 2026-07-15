package site.siredvin.peripheralworks.integrations.projecte

import site.siredvin.peripheralworks.api.IForgeConfigHandler
import net.neoforged.neoforge.common.ModConfigSpec as ForgeConfigSpec

object Configuration : IForgeConfigHandler {

    private var enableTurtleUpgradeConfig: ForgeConfigSpec.BooleanValue? = null
    private var enablePocketUpgradeConfig: ForgeConfigSpec.BooleanValue? = null

    val enableTransmutationTabletTurtleUpgrade: Boolean
        get() = enableTurtleUpgradeConfig?.get() ?: true

    val enableTransmutationTabletPocketUpgrade: Boolean
        get() = enablePocketUpgradeConfig?.get() ?: true

    override val name: String
        get() = "projecte"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableTurtleUpgradeConfig = builder.comment("Enables usage of transmutation tablet as turtle upgrade")
            .define("enableTransmutationTabletTurtleUpgrade", true)
        enablePocketUpgradeConfig = builder.comment("Enables usage of transmutation tablet as pocket upgrade")
            .define("enableTransmutationTabletPocketUpgrade", true)
    }
}
