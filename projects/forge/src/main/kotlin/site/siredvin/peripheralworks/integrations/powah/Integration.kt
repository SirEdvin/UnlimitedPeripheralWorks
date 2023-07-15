package site.siredvin.peripheralworks.integrations.powah

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import owmii.powah.block.ender.AbstractEnderTile
import owmii.powah.lib.block.AbstractEnergyProvider
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy

class Integration : Runnable {

    object GeneratorPluginProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = "powah_extra"

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val blockEntity = level.getBlockEntity(pos)
            if (Configuration.enableGenerator && blockEntity is AbstractEnergyProvider<*>) {
                return GeneratorPlugin(blockEntity)
            }
            if (Configuration.enableEnderCell && blockEntity is AbstractEnderTile<*>) {
                return EnderCellPlugin(blockEntity)
            }
            return null
        }
    }

    override fun run() {
        ComputerCraftProxy.addProvider(GeneratorPluginProvider)
        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)
    }
}
