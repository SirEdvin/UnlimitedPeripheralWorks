package site.siredvin.peripheralworks.client.configurator

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
import site.siredvin.peripheralworks.data.ModText

object NetworkManagerScreenEntry {
    fun open(pos: BlockPos) {
        val minecraft = Minecraft.getInstance()
        if (minecraft.level?.getBlockEntity(pos) is NetworkManagerBlockEntity) {
            minecraft.setScreen(NetworkManagerScreen(pos))
            return
        }
        minecraft.player?.displayClientMessage(ModText.NETWORK_MANAGER_UNAVAILABLE.text, true)
    }
}
