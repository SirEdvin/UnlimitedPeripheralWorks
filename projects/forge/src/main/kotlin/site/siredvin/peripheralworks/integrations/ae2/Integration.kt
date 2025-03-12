package site.siredvin.peripheralworks.integrations.ae2

import appeng.blockentity.grid.AENetworkBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStorageLookup
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorage
import site.siredvin.broccolium.modules.storage.fluid.AgnosticFluidStorageLookup
import site.siredvin.broccolium.modules.storage.fluid.api.AgnosticFluidStorage
import site.siredvin.broccolium.modules.storage.item.AgnosticItemStorageLookup
import site.siredvin.broccolium.modules.storage.item.api.AgnosticItemStorage
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy

class Integration : Runnable {

    companion object {
        @Suppress("UNUSED_PARAMETER")
        fun extractItemStorage(level: Level, pos: BlockPos, entity: BlockEntity?): AgnosticItemStorage? {
            if (entity !is AENetworkBlockEntity) return null
            val inventory = entity.mainNode.grid?.storageService?.inventory ?: return null
            return AEItemStorage(inventory, entity)
        }

        @Suppress("UNUSED_PARAMETER")
        fun extractFluidStorage(level: Level, pos: BlockPos, entity: BlockEntity?): AgnosticFluidStorage? {
            if (entity !is AENetworkBlockEntity) return null
            val inventory = entity.mainNode.grid?.storageService?.inventory ?: return null
            return AEFluidStorage(inventory, entity)
        }

        @Suppress("UNUSED_PARAMETER")
        fun extractEnergyStorage(level: Level, pos: BlockPos, entity: BlockEntity?): AgnosticEnergyStorage? {
            if (entity !is AENetworkBlockEntity) return null
            val energyService = entity.mainNode.grid?.energyService ?: return null
            return AEEnergyStorage(energyService, entity)
        }
    }

    override fun run() {
        if (Configuration.enableStorageIntegrations) {
            AgnosticItemStorageLookup.addItemStorageExtractor(::extractItemStorage)
            AgnosticFluidStorageLookup.addFluidStorageExtractor(::extractFluidStorage)
            AgnosticEnergyStorageLookup.addEnergyStorageExtractor(::extractEnergyStorage)
        }
        if (Configuration.enableMEInterface) {
            ComputerCraftProxy.addProvider(MENetworkBlockPlugin.Provider)
        }
    }
}
