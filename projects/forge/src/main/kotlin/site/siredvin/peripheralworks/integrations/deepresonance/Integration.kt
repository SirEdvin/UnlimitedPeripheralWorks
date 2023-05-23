package site.siredvin.peripheralworks.integrations.deepresonance

import mcjty.deepresonance.modules.core.block.ResonatingCrystalTileEntity
import mcjty.deepresonance.modules.generator.block.GeneratorControllerTileEntity
import mcjty.deepresonance.modules.generator.block.GeneratorPartTileEntity
import mcjty.deepresonance.modules.tank.blocks.TankTileEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraftforge.common.capabilities.ForgeCapabilities
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.extra.plugins.ForgeFluidStoragePlugin
import site.siredvin.peripheralium.extra.plugins.PeripheralPluginUtils
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy

class Integration: Runnable {

    object CrystalProvider: PeripheralPluginProvider {
        override val pluginType: String
            get() = "resonating_crystal"
        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!Configuration.enableResonatingCrystal)
                return null
            val blockEntity = level.getBlockEntity(pos)
            if (blockEntity is ResonatingCrystalTileEntity)
                return CrystalPlugin(blockEntity)
            return null
        }
    }

    object GeneratorPartProvider: PeripheralPluginProvider {
        override val pluginType: String
            get() = "generator"
        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!Configuration.enableGeneratorPart)
                return null
            val blockEntity = level.getBlockEntity(pos)
            if (blockEntity is GeneratorPartTileEntity)
                return GeneratorPartBlockPlugin(blockEntity)
            return null
        }
    }

    object TankStorageProvider: PeripheralPluginProvider {
        override val pluginType: String
            get() = PeripheralPluginUtils.TYPES.FLUID_STORAGE

        override val priority: Int
            get() = 50

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!Configuration.enableTank)
                return null
            val blockEntity = level.getBlockEntity(pos) ?: return null
            if (blockEntity is TankTileEntity)
                return TankPlugin(blockEntity)
            return null
        }

    }

    override fun run() {
        ComputerCraftProxy.addProvider(CrystalProvider)
        ComputerCraftProxy.addProvider(GeneratorPartProvider)
        ComputerCraftProxy.addProvider(TankStorageProvider)
        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)
    }
}