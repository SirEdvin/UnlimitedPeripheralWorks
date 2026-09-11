package site.siredvin.peripheralworks.common.configuration

import site.siredvin.peripheralworks.api.IForgeConfigHandler
import net.neoforged.neoforge.common.ModConfigSpec as ForgeConfigSpec

interface IntegrationConfiguration : IForgeConfigHandler {
    val modID: String

    fun <T> ForgeConfigSpec.ConfigValue<T>?.getOrDefault(default: T): T = if (this == null || !ConfigHolder.isLoaded) default else get()
}
