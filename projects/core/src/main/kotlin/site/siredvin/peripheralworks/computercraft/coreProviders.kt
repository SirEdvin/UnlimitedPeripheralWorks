package site.siredvin.peripheralworks.computercraft

import com.google.common.collect.Iterators
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

object StorageProvider: PeripheralPluginProvider {
    override val pluginType: String
        get() = PeripheralPluginUtils.TYPES.ITEM_STORAGE
    override val priority: Int
        get() = 200
    override val conflictWith: Set<String>
        get() = setOf(PeripheralPluginUtils.TYPES.INVENTORY)

    override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
        if (!PeripheralWorksConfig.enableGenericItemStorage)
            return null
        val storage = ExtractorProxy.extractStorage(level, pos) ?: return null
        if (Iterators.size(storage.getItems()) == 0)
            return null
        if (storage is SlottedStorage)
            return InventoryPlugin(level, storage)
        return ItemStoragePlugin(storage, level)
    }
}