package site.siredvin.peripheralworks.integrations.automobility

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {

    private var enableAutomobilityConfig: ForgeConfigSpec.BooleanValue? = null

    val enableAutomobile: Boolean
        get() = enableAutomobilityConfig?.get() ?: true

    override val name: String
        get() = "automobility"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableAutomobilityConfig = builder.comment("Enables automobile entity integration")
            .define("enableAutomobile", true)
    }
}
