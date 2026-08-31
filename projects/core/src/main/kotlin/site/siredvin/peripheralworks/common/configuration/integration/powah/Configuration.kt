package site.siredvin.peripheralworks.common.configuration.integration.powah

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {

    private var enableEnergyConfig: ForgeConfigSpec.BooleanValue? = null
    private var enableGeneraotrConfig: ForgeConfigSpec.BooleanValue? = null
    private var enableEnergyCellConfig: ForgeConfigSpec.BooleanValue? = null
    private var enableReactorConfig: ForgeConfigSpec.BooleanValue? = null
    private var enableRedstoneControlConfig: ForgeConfigSpec.BooleanValue? = null

    val enableEnergy: Boolean
        get() = enableEnergyConfig?.get() ?: true

    val enableGenerator: Boolean
        get() = enableGeneraotrConfig?.get() ?: true

    val enableEnderCell: Boolean
        get() = enableEnergyCellConfig?.get() ?: true

    val enableReactor: Boolean
        get() = enableReactorConfig?.get() ?: true

    val enableRedstoneControl: Boolean
        get() = enableRedstoneControlConfig?.get() ?: true

    override val name: String
        get() = "powah"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableEnergyConfig = builder.comment("Enabled energy integration")
            .define("enableEnergy", true)
        enableGeneraotrConfig = builder.comment("Enables generators integration")
            .define("enableGenerator", true)
        enableEnergyCellConfig = builder.comment("Enables ender cell integration")
            .define("enableEnderCell", true)
        enableReactorConfig = builder.comment("Enables reactor integration")
            .define("enableReactor", true)
        enableRedstoneControlConfig = builder.comment("Enables redstone control integration")
            .define("enableRedstoneControl", true)
    }
}
