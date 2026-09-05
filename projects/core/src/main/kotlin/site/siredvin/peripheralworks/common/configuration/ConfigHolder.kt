package site.siredvin.peripheralworks.common.configuration

import net.minecraftforge.common.ForgeConfigSpec

object ConfigHolder {
    private var configuredSpec: ForgeConfigSpec? = null
    private var configuredCommon: PeripheralWorksConfig.CommonConfig? = null

    internal val isLoaded: Boolean
        get() = configuredSpec?.isLoaded == true

    val commonSpec: ForgeConfigSpec
        get() = checkNotNull(configuredSpec) { "Peripheral Works configuration has not been initialized" }
    val commonConfig: PeripheralWorksConfig.CommonConfig
        get() = checkNotNull(configuredCommon) { "Peripheral Works configuration has not been initialized" }

    fun initialize(isModPresent: (String) -> Boolean) {
        check(configuredSpec == null) { "Peripheral Works configuration is already initialized" }
        val integrationConfigurations = IntegrationConfigurationDiscovery.discover(isModPresent)
        val (key, value) = ForgeConfigSpec.Builder()
            .configure { builder: ForgeConfigSpec.Builder -> PeripheralWorksConfig.CommonConfig(builder, integrationConfigurations) }
        configuredCommon = key
        configuredSpec = value
    }
}
