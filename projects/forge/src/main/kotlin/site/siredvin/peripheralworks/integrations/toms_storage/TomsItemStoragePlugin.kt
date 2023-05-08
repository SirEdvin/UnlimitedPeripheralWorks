package site.siredvin.peripheralworks.integrations.toms_storage

import com.tom.storagemod.tile.InventoryConnectorBlockEntity
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.storage.Storage
import site.siredvin.peripheralium.extra.plugins.AbstractItemStoragePlugin
import site.siredvin.peripheralium.storage.ItemHandlerWrapper

class TomsItemStoragePlugin(private val target: InventoryConnectorBlockEntity): AbstractItemStoragePlugin() {
    override val level: Level
        get() = target.level!!
    override val storage: Storage = ItemHandlerWrapper(target.inventory.resolve().get())
}