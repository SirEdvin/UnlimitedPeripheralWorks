package site.siredvin.peripheralworks.integrations.alloy_forgery

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.integration.AlloyForgeryConfiguration
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import wraith.alloyforgery.block.ForgeControllerBlockEntity

class Integration : Runnable {

    object ForgeControllerPluginProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = "alloy_forge"

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val blockEntity = level.getBlockEntity(pos)
            if (AlloyForgeryConfiguration.enableAlloyForgery && blockEntity is ForgeControllerBlockEntity) {
                return ForgeControllerPlugin(blockEntity)
            }
            return null
        }
    }
    override fun run() {
        ComputerCraftProxy.addProvider(ForgeControllerPluginProvider)
    }
}
