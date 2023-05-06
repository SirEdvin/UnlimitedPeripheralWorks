package site.siredvin.peripheralworks.integrations.integrateddynamics

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.IConfigHandler

object Configuration: IConfigHandler {

    private var ENABLE_VARIABLE_STORE: ForgeConfigSpec.BooleanValue? = null
    private var ENABLE_COMPUTER_ASPECT: ForgeConfigSpec.BooleanValue? = null

    val enableVariableStore: Boolean
        get() = ENABLE_VARIABLE_STORE?.get() ?: true

    val enableComputerAspect: Boolean
        get() = ENABLE_COMPUTER_ASPECT?.get() ?: true

    override val name: String
        get() = "integrateddynamics"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        ENABLE_VARIABLE_STORE = builder.comment("Enables variable store integration").define("enableVariableStore", true)
        ENABLE_COMPUTER_ASPECT = builder.comment("Enables computer aspect for block reader").define("enableComputerAspect", true)
    }
}