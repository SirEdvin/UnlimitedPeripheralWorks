package site.siredvin.peripheralworks.common.configuration

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object ConfigHolder {
    private var configuredSpec: ForgeConfigSpec? = null
    private var configuredCommon: PeripheralWorksConfig.CommonConfig? = null

    val commonSpec: ForgeConfigSpec
        get() = checkNotNull(configuredSpec) { "Peripheral Works configuration has not been initialized" }
    val commonConfig: PeripheralWorksConfig.CommonConfig
        get() = checkNotNull(configuredCommon) { "Peripheral Works configuration has not been initialized" }

    fun initialize(integrationConfigurations: List<IForgeConfigHandler>) {
        check(configuredSpec == null) { "Peripheral Works configuration is already initialized" }
        val (key, value) = ForgeConfigSpec.Builder()
            .configure { builder: ForgeConfigSpec.Builder -> PeripheralWorksConfig.CommonConfig(builder, integrationConfigurations) }
        configuredCommon = key
        configuredSpec = value
    }
}
