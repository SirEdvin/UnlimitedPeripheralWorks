package site.siredvin.peripheralworks.integrations.ae2

import appeng.blockentity.grid.AENetworkBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import site.siredvin.peripheralium.storages.energy.EnergyStorage
import site.siredvin.peripheralium.storages.energy.EnergyStorageExtractor
import site.siredvin.peripheralium.storages.fluid.FluidStorage
import site.siredvin.peripheralium.storages.fluid.FluidStorageExtractor
import site.siredvin.peripheralium.storages.item.ItemStorage
import site.siredvin.peripheralium.storages.item.ItemStorageExtractor

class Integration : Runnable {

    companion object {
        fun extractItemStorage(level: Level, pos: BlockPos, entity: BlockEntity?): ItemStorage? {
            if (entity !is AENetworkBlockEntity) return null
            val inventory = entity.mainNode.grid?.storageService?.inventory ?: return null
            return AEItemStorage(inventory, entity)
        }

        fun extractFluidStorage(level: Level, pos: BlockPos, entity: BlockEntity?): FluidStorage? {
            if (entity !is AENetworkBlockEntity) return null
            val inventory = entity.mainNode.grid?.storageService?.inventory ?: return null
            return AEFluidStorage(inventory, entity)
        }

        fun extractEnergyStorage(level: Level, pos: BlockPos, entity: BlockEntity?): EnergyStorage? {
            if (entity !is AENetworkBlockEntity) return null
            val energyService = entity.mainNode.grid?.energyService ?: return null
            return AEEnergyStorage(energyService, entity)
        }
    }

    override fun run() {
        if (Configuration.enableStorageIntegrations) {
            ItemStorageExtractor.addStorageExtractor(::extractItemStorage)
            FluidStorageExtractor.addFluidStorageExtractor(::extractFluidStorage)
            EnergyStorageExtractor.addEnergyStorageExtractor(::extractEnergyStorage)
        }
    }
}
