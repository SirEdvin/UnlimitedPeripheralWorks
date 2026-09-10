package site.siredvin.peripheralworks.common.configuration

import net.neoforged.neoforge.common.ModConfigSpec

object ConfigHolder {
    private var configuredSpec: ModConfigSpec? = null
    private var configuredCommon: PeripheralWorksConfig.CommonConfig? = null

    internal val isLoaded: Boolean
        get() = configuredSpec?.isLoaded == true

    val commonSpec: ModConfigSpec
        get() = checkNotNull(configuredSpec) { "Peripheral Works configuration has not been initialized" }
    val commonConfig: PeripheralWorksConfig.CommonConfig
        get() = checkNotNull(configuredCommon) { "Peripheral Works configuration has not been initialized" }

    fun initialize(isModPresent: (String) -> Boolean) {
        check(configuredSpec == null) { "Peripheral Works configuration is already initialized" }
        val integrationConfigurations = IntegrationConfigurationDiscovery.discover(isModPresent)
        val (key, value) = ModConfigSpec.Builder()
            .configure { builder: ModConfigSpec.Builder -> PeripheralWorksConfig.CommonConfig(builder, integrationConfigurations) }
        configuredCommon = key
        configuredSpec = value
    }
}
