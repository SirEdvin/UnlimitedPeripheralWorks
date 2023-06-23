package site.siredvin.peripheralworks.computercraft.plugins

import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IOwnedPeripheral
import site.siredvin.peripheralium.extra.plugins.AbstractInventoryPlugin
import site.siredvin.peripheralium.storages.item.SlottedItemStorage
import site.siredvin.peripheralium.util.representation.LuaRepresentation
import site.siredvin.peripheralium.util.representation.RepresentationMode
import site.siredvin.peripheralworks.common.blockentity.AbstractItemPedestalBlockEntity

class PedestalInventoryPlugin<T : IOwnedPeripheral<*>>(private val blockEntity: AbstractItemPedestalBlockEntity<T>) : AbstractInventoryPlugin() {
    override val level: Level
        get() = blockEntity.level!!
    override val storage: SlottedItemStorage
        get() = blockEntity.storage

    override fun getItemDetailImpl(slot: Int): Map<String, *>? {
        val stack = storage.getItem(slot)
        if (stack.isEmpty) {
            return null
        }
        return LuaRepresentation.forItemStack(stack, RepresentationMode.FULL)
    }
}
