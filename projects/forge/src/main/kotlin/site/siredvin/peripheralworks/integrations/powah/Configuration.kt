package site.siredvin.peripheralworks.integrations.powah

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.config.IConfigHandler

object Configuration : IConfigHandler {

    private var ENABLE_GENERATOR: ForgeConfigSpec.BooleanValue? = null
    private var ENABLE_ENDER_CELL: ForgeConfigSpec.BooleanValue? = null

    val enableGenerator: Boolean
        get() = ENABLE_GENERATOR?.get() ?: true

    val enableEnderCell: Boolean
        get() = ENABLE_ENDER_CELL?.get() ?: true

    override val name: String
        get() = "powah"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        ENABLE_GENERATOR = builder.comment("Enables generators integration")
            .define("enableGenerator", true)
        ENABLE_ENDER_CELL = builder.comment("Enables ender cell integration")
            .define("enableEnderCell", true)
    }
}
