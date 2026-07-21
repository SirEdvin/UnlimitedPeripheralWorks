package site.siredvin.peripheralworks.subsystem.entityperipheral

import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import site.siredvin.broccolium.modules.storage.base.api.SlottedAgnosticStorage
import site.siredvin.broccolium.modules.storage.item.AgnosticItemStorageLookup
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
        val entityStorage = AgnosticItemStorageLookup.extractFromUnknown(entity.level(), entity, null) ?: return null
        if (entityStorage is SlottedAgnosticStorage<ItemStack, Int>) {
            return InventoryPlugin(entity.level(), entityStorage, PeripheralWorksConfig.itemStorageTransferLimit)
        }
        return ItemStoragePlugin(entityStorage, entity.level(), PeripheralWorksConfig.itemStorageTransferLimit)
    }
}
