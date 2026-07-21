package site.siredvin.peripheralworks.computercraft.plugins

import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import site.siredvin.broccolium.modules.storage.base.api.SlottedAgnosticStorage
import site.siredvin.peripheralworks.common.blockentity.AbstractItemPedestalBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.tweakium.modules.peripheral.api.IOwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation
import site.siredvin.tweakium.modules.peripheral.representation.RepresentationMode
import site.siredvin.tweakium.modules.plugins.AbstractInventoryPlugin

class PedestalInventoryPlugin<T : IOwnedPeripheral<*>>(private val blockEntity: AbstractItemPedestalBlockEntity<T>) : AbstractInventoryPlugin() {
    override val level: Level
        get() = blockEntity.level!!
    override val storage: SlottedAgnosticStorage<ItemStack, Int>
        get() = blockEntity.storage

    override fun getItemDetailImpl(slot: Int): Map<String, *>? {
        val stack = storage.get(slot)
        if (stack.isEmpty) {
            return null
        }
        return LuaRepresentation.forItemStack(stack, RepresentationMode.FULL)
    }

    override val inventoryTransferLimit: Int
        get() = PeripheralWorksConfig.itemStorageTransferLimit
}
