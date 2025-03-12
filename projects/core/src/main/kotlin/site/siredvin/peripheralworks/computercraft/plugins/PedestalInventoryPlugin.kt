package site.siredvin.peripheralworks.computercraft.plugins

import net.minecraft.world.level.Level
import site.siredvin.broccolium.modules.storage.item.api.SlottedAgnosticItemStorage
import site.siredvin.peripheralworks.common.blockentity.AbstractItemPedestalBlockEntity
import site.siredvin.tweakium.modules.peripheral.api.IOwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation
import site.siredvin.tweakium.modules.peripheral.representation.RepresentationMode
import site.siredvin.tweakium.modules.plugins.AbstractInventoryPlugin

class PedestalInventoryPlugin<T : IOwnedPeripheral<*>>(private val blockEntity: AbstractItemPedestalBlockEntity<T>) : AbstractInventoryPlugin() {
    override val level: Level
        get() = blockEntity.level!!
    override val storage: SlottedAgnosticItemStorage
        get() = blockEntity.storage

    override fun getItemDetailImpl(slot: Int): Map<String, *>? {
        val stack = storage.getItem(slot)
        if (stack.isEmpty) {
            return null
        }
        return LuaRepresentation.forItemStack(stack, RepresentationMode.FULL)
    }
}
