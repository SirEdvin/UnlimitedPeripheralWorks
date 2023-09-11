package site.siredvin.peripheralworks.integrations.modern_industrialization

import aztech.modern_industrialization.api.machine.holder.EnergyComponentHolder
import site.siredvin.peripheralium.storages.energy.EnergyStorageExtractor
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy

class Integration : Runnable {
    override fun run() {
        EnergyStorageExtractor.addEnergyStorageExtractor(
            EnergyStorageExtractor.EnergyStorageExtractor { _, _, entity ->
                if (entity == null || !Configuration.enableEnergyStorage) return@EnergyStorageExtractor null
                if (entity is EnergyComponentHolder) {
                    return@EnergyStorageExtractor MIEnergyStorage(entity.energyComponent)
                }
                return@EnergyStorageExtractor null
            },
        )
        ComputerCraftProxy.addProvider(CraftingMachinePlugin.Provider)
    }
}
