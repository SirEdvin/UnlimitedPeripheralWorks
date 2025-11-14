package site.siredvin.peripheralworks.integrations.toms_storage

import com.tom.storagemod.tile.InventoryConnectorBlockEntity
import net.minecraft.world.level.Level
import site.siredvin.broccolium.modules.storage.item.AgnosticItemHandlerWrapper
import site.siredvin.broccolium.modules.storage.item.api.SlottedAgnosticItemStorage
import site.siredvin.tweakium.modules.plugins.AbstractInventoryPlugin

class TomsItemStoragePlugin(private val target: InventoryConnectorBlockEntity) : AbstractInventoryPlugin() {
    override val level: Level
        get() = target.level!!
    override val storage: SlottedAgnosticItemStorage = AgnosticItemHandlerWrapper(target.inventory.resolve().get())
}
