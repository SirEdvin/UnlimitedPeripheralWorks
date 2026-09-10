package site.siredvin.peripheralworks.common.configuration.integration

import net.neoforged.neoforge.common.ModConfigSpec
import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration

object PowahConfiguration : IntegrationConfiguration {

    override val modID: String
        get() = "powah"

    private var enableEnergyConfig: ModConfigSpec.BooleanValue? = null
    private var enableGeneraotrConfig: ModConfigSpec.BooleanValue? = null
    private var enableEnergyCellConfig: ModConfigSpec.BooleanValue? = null
    private var enableReactorConfig: ModConfigSpec.BooleanValue? = null
    private var enableRedstoneControlConfig: ModConfigSpec.BooleanValue? = null

    val enableEnergy: Boolean
        get() = enableEnergyConfig.getOrDefault(true)

    val enableGenerator: Boolean
        get() = enableGeneraotrConfig.getOrDefault(true)

    val enableEnderCell: Boolean
        get() = enableEnergyCellConfig.getOrDefault(true)

    val enableReactor: Boolean
        get() = enableReactorConfig.getOrDefault(true)

    val enableRedstoneControl: Boolean
        get() = enableRedstoneControlConfig.getOrDefault(true)

    override val name: String
        get() = "powah"

    override fun addToConfig(builder: ModConfigSpec.Builder) {
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
