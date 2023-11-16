package site.siredvin.peripheralworks.integrations.lifts

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.IConfigHandler

object Configuration : IConfigHandler {

    private var ENABLE_LIFT: ForgeConfigSpec.BooleanValue? = null

    val enableLift: Boolean
        get() = ENABLE_LIFT?.get() ?: true

    override val name: String
        get() = "lift"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        ENABLE_LIFT = builder.comment("Enables lift integration").define("enableLift", true)
    }
}
