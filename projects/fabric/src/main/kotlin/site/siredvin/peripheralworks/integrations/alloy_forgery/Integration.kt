package site.siredvin.peripheralworks.integrations.alloy_forgery

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import wraith.alloyforgery.block.ForgeControllerBlockEntity

class Integration : Runnable {

    object ForgeControllerPluginProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = "alloy_forge"

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val blockEntity = level.getBlockEntity(pos)
            if (Configuration.enableAlloyForgery && blockEntity is ForgeControllerBlockEntity) {
                return ForgeControllerPlugin(blockEntity)
            }
            return null
        }
    }
    override fun run() {
        ComputerCraftProxy.addProvider(ForgeControllerPluginProvider)
        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)
    }
}
