package site.siredvin.peripheralworks.api

import net.neoforged.neoforge.common.ModConfigSpec
import site.siredvin.broccolium.modules.base.api.IConfigHandler

interface IForgeConfigHandler : IConfigHandler {
    fun addToConfig(builder: ModConfigSpec.Builder)
}
