package site.siredvin.peripheralworks.common

import dan200.computercraft.shared.util.InventoryUtil
import dan200.computercraft.shared.util.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.minecraft.world.Container
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import site.siredvin.peripheralworks.api.CCItemStorageExtractor

object ExtractorProxy {

    private val ADDITIONAL_CC_ITEM_STORAGE_EXTRACTORS: MutableList<CCItemStorageExtractor> = mutableListOf()

    init {
        addCCItemStorageExtractor(MinecartHelpers::minecartExtractor)
    }

    fun addCCItemStorageExtractor(extractor: CCItemStorageExtractor) {
        ADDITIONAL_CC_ITEM_STORAGE_EXTRACTORS.add(extractor)
    }

    fun extractFluidStorage(level: Level, obj: Any?): Storage<FluidVariant>? {
        if (obj is BlockEntity) {
            if (obj.isRemoved)
                return null
            return FluidStorage.SIDED.find(obj.level, obj.blockPos, null)
        }
        return null
    }

    fun extractItemStorage(level: Level, obj: Any?): Storage<ItemVariant>? {
        if (obj is BlockEntity) {
            if (obj.isRemoved)
                return null
            return net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.find(obj.level, obj.blockPos, null)
        }
        return null
    }

    fun extractCCItemStorage(level: Level, obj: Any?): ItemStorage? {
        for (extractor in this.ADDITIONAL_CC_ITEM_STORAGE_EXTRACTORS) {
            val result = extractor.extract(level, obj)
            if (result != null)
                return result
        }

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