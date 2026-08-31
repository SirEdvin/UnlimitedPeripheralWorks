package site.siredvin.peripheralworks.integrations.fluxnetworks

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.integration.fluxnetworks.Configuration
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import sonar.fluxnetworks.common.device.TileFluxController

class Integration : Runnable {

    object FluxNetworksProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = "flux_networks"
        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val blockEntity = level.getBlockEntity(pos) ?: return null
            if (blockEntity is TileFluxController && Configuration.enableFluxController) {
                return FluxControllerPlugin(blockEntity)
            }
            return null
        }
    }

    override fun run() {
        ComputerCraftProxy.addProvider(FluxNetworksProvider)
    }
}
