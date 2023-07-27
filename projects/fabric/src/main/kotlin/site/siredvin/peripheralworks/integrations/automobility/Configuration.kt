package site.siredvin.peripheralworks.integrations.automobility

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.config.IConfigHandler

object Configuration : IConfigHandler {

    private var ENABLE_AUTOMOBILE_ENTITY: ForgeConfigSpec.BooleanValue? = null

    val enableAutomobile: Boolean
        get() = ENABLE_AUTOMOBILE_ENTITY?.get() ?: true

    override val name: String
        get() = "automobility"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        ENABLE_AUTOMOBILE_ENTITY = builder.comment("Enables automobile entity integration")
            .define("enableAutomobile", true)
    }
}
