package site.siredvin.peripheralworks.common

import dan200.computercraft.shared.util.InventoryUtil
import dan200.computercraft.shared.util.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.minecraft.world.Container
import net.minecraft.world.level.block.entity.BlockEntity

object ExtractorProxy {
    fun extractFluidStorage(obj: Any?): Storage<FluidVariant>? {
        if (obj is BlockEntity) {
            if (obj.isRemoved)
                return null
            return FluidStorage.SIDED.find(obj.level, obj.blockPos, null)
        }
        return null
    }

    fun extractItemStorage(obj: Any?): Storage<ItemVariant>? {
        if (obj is BlockEntity) {
            if (obj.isRemoved)
                return null
            return net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.find(obj.level, obj.blockPos, null)
        }
        return null
    }

    fun extractCCItemStorage(obj: Any?): ItemStorage? {
        if (obj is BlockEntity) {
            if (obj.isRemoved)
                return null
            val inventory = InventoryUtil.getInventory(obj)
            if (inventory != null)
                return ItemStorage.wrap(inventory)
        }
        if (obj is Container)
            return ItemStorage.wrap(obj)
        return null
    }
}