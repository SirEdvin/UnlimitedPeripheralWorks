package site.siredvin.peripheralworks.subsystem.entityperipheral

import net.minecraft.world.entity.Entity
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.extra.plugins.InventoryPlugin
import site.siredvin.peripheralium.extra.plugins.ItemStoragePlugin
import site.siredvin.peripheralium.extra.plugins.PeripheralPluginUtils
import site.siredvin.peripheralium.storages.item.ItemStorageExtractor
import site.siredvin.peripheralium.storages.item.SlottedItemStorage
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig

object GenericEntityStorageProvider : EntityPeripheralPluginProvider {
    override val pluginType: String
        get() = "storage"
    override val conflictWith: Set<String>
        get() = setOf(PeripheralPluginUtils.Type.INVENTORY, PeripheralPluginUtils.Type.ITEM_STORAGE)

    override fun provide(entity: Entity): IPeripheralPlugin? {
        val entityStorage = ItemStorageExtractor.extractStorage(entity.level(), entity) ?: return null
        if (entityStorage is SlottedItemStorage) {
            return InventoryPlugin(entity.level(), entityStorage)
        }
        return ItemStoragePlugin(entityStorage, entity.level(), PeripheralWorksConfig.itemStorageTransferLimit)
    }
}
