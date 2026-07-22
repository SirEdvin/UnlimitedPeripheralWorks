package site.siredvin.peripheralworks.common.configuration

import net.minecraftforge.common.ForgeConfigSpec

object ConfigHolder {
    var commonSpec: ForgeConfigSpec
    var commonConfig: PeripheralWorksConfig.CommonConfig
    init {
        val (key, value) = ForgeConfigSpec.Builder()
            .configure { builder: ForgeConfigSpec.Builder -> PeripheralWorksConfig.CommonConfig(builder) }
        commonConfig = key
        commonSpec = value
    }
}
