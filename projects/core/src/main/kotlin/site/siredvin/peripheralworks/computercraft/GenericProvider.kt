package site.siredvin.peripheralworks.computercraft

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.extra.plugins.*
import site.siredvin.peripheralium.storages.energy.EnergyStorageExtractor
import site.siredvin.peripheralium.storages.fluid.FluidStorageExtractor
import site.siredvin.peripheralium.storages.item.ItemStorageExtractor
import site.siredvin.peripheralium.storages.item.SlottedItemStorage
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig

object StorageProvider : PeripheralPluginProvider {
    override val pluginType: String
        get() = "storage"
    override val conflictWith: Set<String>
        get() = setOf(PeripheralPluginUtils.Type.INVENTORY, PeripheralPluginUtils.Type.ITEM_STORAGE)

    override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
        if (!PeripheralWorksConfig.enableGenericItemStorage && !PeripheralWorksConfig.enableGenericInventory) {
            return null
        }
        val storage = ItemStorageExtractor.extractStorage(level, pos, level.getBlockEntity(pos)) ?: return null
        if (storage is SlottedItemStorage && PeripheralWorksConfig.enableGenericInventory && storage.size != 0) {
            return InventoryPlugin(level, storage)
        }
        if (PeripheralWorksConfig.enableGenericItemStorage) {
            return ItemStoragePlugin(storage, level, PeripheralWorksConfig.itemStorageTransferLimit)
        }
        return null
    }
}

object FluidStorageProvider : PeripheralPluginProvider {
    override val pluginType: String
        get() = PeripheralPluginUtils.Type.FLUID_STORAGE

    override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
        if (!PeripheralWorksConfig.enableGenericFluidStorage) return null
        val storage = FluidStorageExtractor.extractFluidStorage(level, pos, level.getBlockEntity(pos)) ?: return null
        return FluidStoragePlugin(level, storage, PeripheralWorksConfig.fluidStorageTransferLimit)
    }
}

object EnergyStorageProvider : PeripheralPluginProvider {
    override val pluginType: String
        get() = PeripheralPluginUtils.Type.ENERGY_STORAGE

    override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
        if (!PeripheralWorksConfig.enableGenericEnergyStorage) return null
        val storage = EnergyStorageExtractor.extractEnergyStorage(level, pos, level.getBlockEntity(pos)) ?: return null
        return EnergyPlugin(storage)
    }
}
