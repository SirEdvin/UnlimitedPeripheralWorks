package site.siredvin.peripheralworks.integrations.team_reborn_energy

import dan200.computercraft.api.ComputerCraftAPI
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStorageLookup
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorage
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.EnergyRefuelHandler
import team.reborn.energy.api.EnergyStorage

class Integration : Runnable {

    companion object {
        @Suppress("UNUSED_PARAMETER")
        fun extractEnergyStorage(level: Level, pos: BlockPos, entity: BlockEntity?, direction: Direction?): AgnosticEnergyStorage? {
            var energyStorage = EnergyStorage.SIDED.find(level, pos, null)
            if (energyStorage == null) {
                energyStorage = EnergyStorage.SIDED.find(level, pos, direction) ?: return null
            }
            return EnergyStorageWrapper(energyStorage)
        }
    }

    override fun run() {
        if (Configuration.enableEnergyStorage) {
            AgnosticEnergyStorageLookup.addBlockLookup(::extractEnergyStorage)
        }
        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)
        ComputerCraftAPI.registerRefuelHandler(EnergyRefuelHandler)
    }
}
