package site.siredvin.peripheralworks.integrations.toms_storage

import com.tom.storagemod.tile.InventoryConnectorBlockEntity
import net.minecraft.world.level.Level
import site.siredvin.broccolium.modules.storage.item.AgnosticItemHandlerWrapper
import site.siredvin.broccolium.modules.storage.item.api.AgnosticItemStorage
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.tweakium.modules.plugins.AbstractItemStoragePlugin

class TomsItemStoragePlugin(private val target: InventoryConnectorBlockEntity) : AbstractItemStoragePlugin() {
    override val itemStorageTransferLimit: Int
        get() = PeripheralWorksConfig.itemStorageTransferLimit
    override val level: Level
        get() = target.level!!
    override val storage: AgnosticItemStorage = AgnosticItemHandlerWrapper(target.inventory.resolve().get())
}
