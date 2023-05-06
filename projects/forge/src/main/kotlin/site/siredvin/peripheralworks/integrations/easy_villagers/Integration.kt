package site.siredvin.peripheralworks.integrations.easy_villagers

import de.maxhenkel.easyvillagers.blocks.tileentity.AutoTraderTileentity
import de.maxhenkel.easyvillagers.blocks.tileentity.TraderTileentityBase
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.extra.plugins.PeripheralPluginUtils
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy

class Integration: Runnable {

    object AutoTraderProvider: PeripheralPluginProvider {
        override val pluginType: String
            get() = "easy_villager_trader"
        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!Configuration.enableAutoTrader)
                return null
            val blockEntity = level.getBlockEntity(pos)
            if (blockEntity is AutoTraderTileentity)
                return AutoTraderPlugin(blockEntity)
            if (blockEntity is TraderTileentityBase)
                return TraderPlugin(blockEntity)
            return null
        }

    }

    override fun run() {
        // TODO: there is no integration for storage, but is was, so CC are able to capture it but not me?
        ComputerCraftProxy.addProvider(AutoTraderProvider)
        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)
    }
}