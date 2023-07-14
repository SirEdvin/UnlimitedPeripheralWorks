package site.siredvin.peripheralworks.integrations.universal_shops

import eu.pb4.universalshops.registry.TradeShopBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy

class Integration : Runnable {

    object UniversalShopPluginProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = "universal_shop"

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val blockEntity = level.getBlockEntity(pos)
            if (Configuration.enableShops && blockEntity is TradeShopBlockEntity) {
                return UniversalShopPlugin(blockEntity)
            }
            return null
        }
    }
    override fun run() {
        ComputerCraftProxy.addProvider(UniversalShopPluginProvider)
        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)
    }
}
