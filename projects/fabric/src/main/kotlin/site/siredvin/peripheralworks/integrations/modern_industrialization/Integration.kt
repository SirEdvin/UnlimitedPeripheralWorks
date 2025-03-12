package site.siredvin.peripheralworks.integrations.modern_industrialization

import aztech.modern_industrialization.api.machine.holder.EnergyComponentHolder
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStorageLookup
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorageExtractor
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy

class Integration : Runnable {
    override fun run() {
        AgnosticEnergyStorageLookup.addEnergyStorageExtractor(
            AgnosticEnergyStorageExtractor { _, _, entity ->
                if (entity == null || !Configuration.enableEnergyStorage) return@AgnosticEnergyStorageExtractor null
                if (entity is EnergyComponentHolder) {
                    return@AgnosticEnergyStorageExtractor MIEnergyStorage(entity.energyComponent)
                }
                return@AgnosticEnergyStorageExtractor null
            },
        )
        ComputerCraftProxy.addProvider(CraftingMachinePlugin.Provider)
    }
}
