package site.siredvin.peripheralworks.common.configuration

import site.siredvin.peripheralworks.api.IForgeConfigHandler

interface IntegrationConfiguration : IForgeConfigHandler {
    val modID: String
}
