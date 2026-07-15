package site.siredvin.peripheralworks.integrations.naturescompass

import net.neoforged.neoforge.common.ModConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {

    private var enableTurtleUpgradeConfig: ModConfigSpec.BooleanValue? = null
    private var enablePocketUpgradeConfig: ModConfigSpec.BooleanValue? = null

    val enableNaturesCompassTurtleUpgrade: Boolean
        get() = enableTurtleUpgradeConfig?.get() ?: true

    val enableNaturesCompassPocketUpgrade: Boolean
        get() = enablePocketUpgradeConfig?.get() ?: true

    override val name: String
        get() = "naturescompass"

    override fun addToConfig(builder: ModConfigSpec.Builder) {
        enableTurtleUpgradeConfig = builder.comment("Enables usage of natures compass as turtle upgrade")
            .define("enableNaturesCompassTurtleUpgrade", true)
        enablePocketUpgradeConfig = builder.comment("Enables usage of natures compass as pocket upgrade")
            .define("enableNaturesCompassPocketUpgrade", true)
    }
}
