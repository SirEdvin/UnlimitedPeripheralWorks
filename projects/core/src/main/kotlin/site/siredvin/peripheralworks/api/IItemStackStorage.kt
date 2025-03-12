package site.siredvin.peripheralworks.api

import site.siredvin.broccolium.modules.storage.item.api.SlottedAgnosticItemStorage

interface IItemStackStorage : IItemStackHolder {
    val storage: SlottedAgnosticItemStorage
}
