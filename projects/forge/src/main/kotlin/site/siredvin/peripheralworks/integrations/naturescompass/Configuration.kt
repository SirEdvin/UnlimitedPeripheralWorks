package site.siredvin.peripheralworks.integrations.naturescompass

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.config.IConfigHandler

object Configuration : IConfigHandler {

    private var ENABLE_NATURES_COMPASS_TURTLE_UPGRADE: ForgeConfigSpec.BooleanValue? = null
    private var ENABLE_NATURES_COMPASS_POCKET_UPGRADE: ForgeConfigSpec.BooleanValue? = null

    val enableNaturesCompassTurtleUpgrade: Boolean
        get() = ENABLE_NATURES_COMPASS_TURTLE_UPGRADE?.get() ?: true

    val enableNaturesCompassPocketUpgrade: Boolean
        get() = ENABLE_NATURES_COMPASS_POCKET_UPGRADE?.get() ?: true

    override val name: String
        get() = "naturescompass"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        ENABLE_NATURES_COMPASS_TURTLE_UPGRADE = builder.comment("Enables usage of natures compass as turtle upgrade")
            .define("enableNaturesCompassTurtleUpgrade", true)
        ENABLE_NATURES_COMPASS_POCKET_UPGRADE = builder.comment("Enables usage of natures compass as pocket upgrade")
            .define("enableNaturesCompassPocketUpgrade", true)
    }
}
