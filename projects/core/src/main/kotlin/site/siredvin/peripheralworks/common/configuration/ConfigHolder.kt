package site.siredvin.peripheralworks.common.configuration

import net.neoforged.neoforge.common.ModConfigSpec

object ConfigHolder {
    var commonSpec: ModConfigSpec
    var commonConfig: PeripheralWorksConfig.CommonConfig

    init {
        val (key, value) = ModConfigSpec.Builder()
            .configure { builder: ModConfigSpec.Builder -> PeripheralWorksConfig.CommonConfig(builder) }
        commonConfig = key
        commonSpec = value
    }
}
