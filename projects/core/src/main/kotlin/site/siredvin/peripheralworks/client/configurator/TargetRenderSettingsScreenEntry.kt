package site.siredvin.peripheralworks.client.configurator

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import site.siredvin.peripheralworks.common.blockentity.PeripheralProxyBlockEntity
import site.siredvin.peripheralworks.common.blockentity.RemoteObserverBlockEntity
import site.siredvin.peripheralworks.data.ModText

object TargetRenderSettingsScreenEntry {
    fun open(pos: BlockPos) {
        val minecraft = Minecraft.getInstance()
        when (minecraft.level?.getBlockEntity(pos)) {
            is PeripheralProxyBlockEntity, is RemoteObserverBlockEntity -> minecraft.setScreen(TargetRenderSettingsScreen(pos))
            else -> minecraft.player?.displayClientMessage(ModText.TARGET_RENDER_SETTINGS_UNAVAILABLE.text, true)
        }
    }
}
