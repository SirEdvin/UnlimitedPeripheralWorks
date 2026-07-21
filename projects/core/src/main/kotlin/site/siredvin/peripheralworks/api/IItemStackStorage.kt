package site.siredvin.peripheralworks.api

import net.minecraft.world.item.ItemStack
import site.siredvin.broccolium.modules.storage.base.api.SlottedAgnosticStorage

interface IItemStackStorage : IItemStackHolder {
    val storage: SlottedAgnosticStorage<ItemStack, Int>
}
