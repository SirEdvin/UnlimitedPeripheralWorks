package site.siredvin.peripheralworks.integrations.toms_storage

import com.tom.storagemod.tile.InventoryConnectorBlockEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import site.siredvin.broccolium.modules.storage.base.api.SlottedAgnosticStorage
import site.siredvin.broccolium.modules.storage.item.AgnosticItemHandlerWrapper
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.tweakium.modules.plugins.AbstractInventoryPlugin

class TomsItemStoragePlugin(private val target: InventoryConnectorBlockEntity) : AbstractInventoryPlugin() {
    override val level: Level
        get() = target.level!!
    override val storage: SlottedAgnosticStorage<ItemStack, Int> = AgnosticItemHandlerWrapper(target.inventory.resolve().get())
    override val inventoryTransferLimit: Int
        get() = PeripheralWorksConfig.itemStorageTransferLimit
}
