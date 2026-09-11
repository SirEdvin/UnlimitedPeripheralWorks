package site.siredvin.peripheralworks.integrations.ae2

import appeng.me.helpers.IGridConnectedBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import site.siredvin.broccolium.modules.storage.base.api.AgnosticStorage
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStorageLookup
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorage
import site.siredvin.broccolium.modules.storage.fluid.AgnosticFluidStorageLookup
import site.siredvin.broccolium.modules.storage.fluid.api.AgnosticFluidStorage
import site.siredvin.broccolium.modules.storage.item.AgnosticItemStorageLookup
import site.siredvin.peripheralworks.common.configuration.integration.AE2Configuration
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy

class Integration : Runnable {

    companion object {
        @Suppress("UNUSED_PARAMETER")
        fun extractItemStorage(level: Level, pos: BlockPos, entity: BlockEntity?, direction: Direction?): AgnosticStorage<ItemStack, Int>? {
            if (entity !is IGridConnectedBlockEntity) return null
            return AEItemStorage(entity)
        }

        @Suppress("UNUSED_PARAMETER")
        fun extractFluidStorage(level: Level, pos: BlockPos, entity: BlockEntity?, direction: Direction?): AgnosticFluidStorage? {
            if (entity !is IGridConnectedBlockEntity) return null
            return AEFluidStorage(entity)
        }

        @Suppress("UNUSED_PARAMETER")
        fun extractEnergyStorage(level: Level, pos: BlockPos, entity: BlockEntity?, direction: Direction?): AgnosticEnergyStorage? {
            if (entity !is IGridConnectedBlockEntity) return null
            return AEEnergyStorage(entity)
        }
    }

    override fun run() {
        if (AE2Configuration.enableStorageIntegrations) {
            AgnosticItemStorageLookup.addBlockLookup(::extractItemStorage)
            AgnosticFluidStorageLookup.addBlockLookup(::extractFluidStorage)
            AgnosticEnergyStorageLookup.addBlockLookup(::extractEnergyStorage)
        }
        if (AE2Configuration.enableMEInterface) {
            ComputerCraftProxy.addProvider(MENetworkBlockPlugin.Provider)
            ComputerCraftProxy.addProvider(AE2StorageSubscriptionPluginProvider)
            ComputerCraftProxy.addProvider(AE2CraftingJobsPluginProvider)
            ComputerCraftProxy.addProvider(AE2CableObjectProvider)
            ComputerCraftProxy.addProvider(AE2InterfaceObjectProvider)
            ComputerCraftProxy.addProvider(AE2PatternProviderObjectProvider)
            AE2Setup.registerPeripherals()
        }
        AE2Setup.registerAttunement()
    }
}
