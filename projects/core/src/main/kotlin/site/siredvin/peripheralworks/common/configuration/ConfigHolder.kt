package site.siredvin.peripheralworks.common.configuration

import net.minecraftforge.common.ForgeConfigSpec

object ConfigHolder {
    var COMMON_SPEC: ForgeConfigSpec
    var COMMON_CONFIG: PeripheralWorksConfig.CommonConfig

    init {
        val (key, value) = ForgeConfigSpec.Builder()
            .configure { builder: ForgeConfigSpec.Builder -> PeripheralWorksConfig.CommonConfig(builder) }
        COMMON_CONFIG = key
        COMMON_SPEC = value
    }
}