package site.siredvin.peripheralworks.integrations.automobility

import net.neoforged.neoforge.common.ModConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {

    private var enableAutomobilityConfig: ModConfigSpec.BooleanValue? = null

    val enableAutomobile: Boolean
        get() = enableAutomobilityConfig?.get() ?: true

    override val name: String
        get() = "automobility"

    override fun addToConfig(builder: ModConfigSpec.Builder) {
        enableAutomobilityConfig = builder.comment("Enables automobile entity integration")
            .define("enableAutomobile", true)
    }
}
