package site.siredvin.peripheralworks.integrations.additionallanterns

import com.supermartijn642.additionallanterns.LanternBlock
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy

class Integration : Runnable {

    object LanternPluginProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = "additionallanterns"

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val blockState = level.getBlockState(pos)
            if (Configuration.enableLanterns && blockState.block is LanternBlock) {
                return LanternPeripheral(level, pos)
            }
            return null
        }
    }

    override fun run() {
        ComputerCraftProxy.addProvider(LanternPluginProvider)
        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)
    }
}
