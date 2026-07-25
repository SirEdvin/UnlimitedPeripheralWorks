package site.siredvin.peripheralworks.client.configurator

import net.minecraft.client.Minecraft
import site.siredvin.peripheralworks.common.item.UltimateConfigurator
import site.siredvin.peripheralworks.common.setup.Items

object ConfiguratorTargetHistoryScreenEntry {
    fun open() {
        val minecraft = Minecraft.getInstance()
        val stack = minecraft.player?.mainHandItem ?: return
        val configurator = stack.item as? UltimateConfigurator ?: return
        if (stack.`is`(Items.ULTIMATE_CONFIGURATOR.get()) && configurator.getActiveMode(stack) == null) {
            minecraft.setScreen(ConfiguratorTargetHistoryScreen())
        }
    }
}
