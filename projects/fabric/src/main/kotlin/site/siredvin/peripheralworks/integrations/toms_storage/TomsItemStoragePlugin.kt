package site.siredvin.peripheralworks.integrations.toms_storage

import com.tom.storagemod.block.entity.InventoryConnectorBlockEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import site.siredvin.broccolium.modules.storage.base.api.AgnosticStorage
import site.siredvin.broccolium.modules.storage.item.FabricStorageWrapper
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.tweakium.modules.plugins.AbstractItemStoragePlugin

class TomsItemStoragePlugin(private val target: InventoryConnectorBlockEntity) : AbstractItemStoragePlugin() {
    override val itemStorageTransferLimit: Int
        get() = PeripheralWorksConfig.itemStorageTransferLimit
    override val level: Level
        get() = target.level!!
    override val storage: AgnosticStorage<ItemStack, Int> = FabricStorageWrapper(target.inventoryAccess.getPlatformHandler())
}
