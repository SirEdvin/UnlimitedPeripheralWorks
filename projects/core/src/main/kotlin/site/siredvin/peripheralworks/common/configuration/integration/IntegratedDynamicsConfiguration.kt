package site.siredvin.peripheralworks.common.configuration.integration

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration

object IntegratedDynamicsConfiguration : IntegrationConfiguration {

    override val modID: String
        get() = "integrateddynamics"

    private const val DEFAULT_MAX_JSON_SIZE = 4_000

    private var enableVariableStoreConfig: ForgeConfigSpec.BooleanValue? = null
    private var enableComputerAspectConfig: ForgeConfigSpec.BooleanValue? = null
    private var maxJSONSizeConfig: ForgeConfigSpec.IntValue? = null

    val enableVariableStore: Boolean
        get() = enableVariableStoreConfig?.get() ?: true

    val enableComputerAspect: Boolean
        get() = enableComputerAspectConfig?.get() ?: true

    val maxJsonSize: Int
        get() = maxJSONSizeConfig?.get() ?: DEFAULT_MAX_JSON_SIZE

    override val name: String
        get() = "integrateddynamics"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableVariableStoreConfig = builder.comment("Enables variable store integration").define("enableVariableStore", true)
        enableComputerAspectConfig = builder.comment("Enables computer aspect for block reader").define("enableComputerAspect", true)
        maxJSONSizeConfig = builder.comment("Max config for output json for machine reader, can impact server memory").defineInRange("maxJSONSizeConfig", DEFAULT_MAX_JSON_SIZE, 100, Int.MAX_VALUE)
    }
}
