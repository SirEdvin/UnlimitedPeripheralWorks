package site.siredvin.peripheralworks.api

import site.siredvin.peripheralium.storages.item.SlottedItemStorage

interface IItemStackStorage : IItemStackHolder {
    val storage: SlottedItemStorage
}
