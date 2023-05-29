package site.siredvin.peripheralworks.api

import site.siredvin.peripheralium.api.storage.SlottedStorage

interface IItemStackStorage : IItemStackHolder {
    val storage: SlottedStorage
}
