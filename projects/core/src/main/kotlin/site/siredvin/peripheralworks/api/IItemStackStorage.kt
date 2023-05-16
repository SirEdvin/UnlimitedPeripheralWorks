package site.siredvin.peripheralworks.api

import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralium.api.storage.SlottedStorage

interface IItemStackStorage: IItemStackHolder {
    val storage: SlottedStorage
}