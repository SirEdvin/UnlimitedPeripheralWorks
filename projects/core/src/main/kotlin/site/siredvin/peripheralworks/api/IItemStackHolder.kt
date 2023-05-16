package site.siredvin.peripheralworks.api

import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralium.api.storage.SlottedStorage

interface IItemStackHolder {
    val storedStack: ItemStack
    val renderLabel: Boolean
        get() = true
    val renderItem: Boolean
        get() = true
}