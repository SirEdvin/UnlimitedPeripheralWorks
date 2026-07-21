package site.siredvin.peripheralworks.common.configuration

import net.minecraftforge.common.ForgeConfigSpec

object ConfigHolder {
    var commonSpec: ForgeConfigSpec
    var commonConfig: PeripheralWorksConfig.CommonConfig
    var clientSpec: ForgeConfigSpec
    var clientConfig: ClientConfig

    init {
        val (key, value) = ForgeConfigSpec.Builder()
            .configure { builder: ForgeConfigSpec.Builder -> PeripheralWorksConfig.CommonConfig(builder) }
        commonConfig = key
        commonSpec = value

        val (clientKey, clientValue) = ForgeConfigSpec.Builder().configure(::ClientConfig)
        clientConfig = clientKey
        clientSpec = clientValue
    }

    class ClientConfig(builder: ForgeConfigSpec.Builder) {
        val networkManagerSettings: ForgeConfigSpec.ConfigValue<List<String>> = builder
            .comment("Client presentation settings for individual network managers")
            .defineList<String>("networkManagerSettings", ::emptyList) { it is String }
    }
}
