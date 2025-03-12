package site.siredvin.peripheralworks.subsystem.entityperipheral

import net.minecraft.world.entity.Entity
import site.siredvin.broccolium.modules.storage.item.AgnosticItemStorageLookup
import site.siredvin.broccolium.modules.storage.item.api.SlottedAgnosticItemStorage
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import site.siredvin.tweakium.modules.plugins.InventoryPlugin
import site.siredvin.tweakium.modules.plugins.ItemStoragePlugin
import site.siredvin.tweakium.modules.plugins.PeripheralPluginUtils

object GenericEntityStorageProvider : EntityPeripheralPluginProvider {
    override val pluginType: String
        get() = "storage"
    override val conflictWith: Set<String>
        get() = setOf(PeripheralPluginUtils.Type.INVENTORY, PeripheralPluginUtils.Type.ITEM_STORAGE)

    override fun provide(entity: Entity): IPeripheralPlugin? {
        val entityStorage = AgnosticItemStorageLookup.extractStorage(entity.level(), entity) ?: return null
        if (entityStorage is SlottedAgnosticItemStorage) {
            return InventoryPlugin(entity.level(), entityStorage)
        }
        return ItemStoragePlugin(entityStorage, entity.level(), PeripheralWorksConfig.itemStorageTransferLimit)
    }
}
