package site.siredvin.peripheralworks.integrations.team_reborn_energy

import dan200.computercraft.api.ComputerCraftAPI
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import site.siredvin.peripheralium.storages.energy.EnergyStorageExtractor
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import team.reborn.energy.api.EnergyStorage

class Integration : Runnable {

    companion object {
        @Suppress("UNUSED_PARAMETER")
        fun extractEnergyStorage(level: Level, pos: BlockPos, entity: BlockEntity?): site.siredvin.peripheralium.storages.energy.EnergyStorage? {
            val energyStorage = EnergyStorage.SIDED.find(level, pos, Direction.NORTH) ?: return null
            return EnergyStorageWrapper(energyStorage)
        }
    }

    override fun run() {
        if (Configuration.enableEnergyStorage) {
            EnergyStorageExtractor.addEnergyStorageExtractor(::extractEnergyStorage)
        }
        ComputerCraftAPI.registerRefuelHandler(EnergyRefuelHandler)
        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)
    }
}
