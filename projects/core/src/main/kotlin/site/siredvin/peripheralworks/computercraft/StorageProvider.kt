package site.siredvin.peripheralworks.computercraft

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.api.storage.ExtractorProxy
import site.siredvin.peripheralium.api.storage.SlottedStorage
import site.siredvin.peripheralium.extra.plugins.InventoryPlugin
import site.siredvin.peripheralium.extra.plugins.ItemStoragePlugin
import site.siredvin.peripheralium.extra.plugins.PeripheralPluginUtils
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
        val storage = ExtractorProxy.extractStorage(level, pos, level.getBlockEntity(pos)) ?: return null
        if (storage is SlottedStorage && PeripheralWorksConfig.enableGenericInventory && storage.size != 0) {
            return InventoryPlugin(level, storage)
        }
        if (PeripheralWorksConfig.enableGenericItemStorage) {
            return ItemStoragePlugin(storage, level, PeripheralWorksConfig.itemStorageTransferLimit)
        }
        return null
    }
}
