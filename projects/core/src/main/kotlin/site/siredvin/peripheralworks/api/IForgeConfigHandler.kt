package site.siredvin.peripheralworks.api

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.broccolium.modules.base.api.IConfigHandler

interface IForgeConfigHandler : IConfigHandler {
    fun addToConfig(builder: ForgeConfigSpec.Builder)
}
