package site.siredvin.peripheralworks.common.configuration

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

interface IntegrationConfiguration : IForgeConfigHandler {
    val modID: String

    fun <T> ForgeConfigSpec.ConfigValue<T>?.getOrDefault(default: T): T = if (this == null || !ConfigHolder.isLoaded) default else get()
}
