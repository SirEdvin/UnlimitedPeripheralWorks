package site.siredvin.peripheralworks.integrations.toms_storage

import com.tom.storagemod.tile.InventoryConnectorBlockEntity
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.extra.plugins.AbstractItemStoragePlugin
import site.siredvin.peripheralium.storages.item.ItemHandlerWrapper
import site.siredvin.peripheralium.storages.item.ItemStorage
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig

class TomsItemStoragePlugin(private val target: InventoryConnectorBlockEntity) : AbstractItemStoragePlugin() {
    override val itemStorageTransferLimit: Int
        get() = PeripheralWorksConfig.itemStorageTransferLimit
    override val level: Level
        get() = target.level!!
    override val storage: ItemStorage = ItemHandlerWrapper(target.inventory.resolve().get())
}
