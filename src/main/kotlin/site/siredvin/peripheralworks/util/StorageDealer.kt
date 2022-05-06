package site.siredvin.peripheralworks.util

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.minecraft.world.level.block.entity.BlockEntity

object StorageDealer {
    fun extractFluidStorage(obj: Any?): Storage<FluidVariant>? {
        if (obj is BlockEntity) {
            if (obj.isRemoved)
                return null
            return FluidStorage.SIDED.find(obj.level, obj.blockPos, null)
        }
        return null
    }
}