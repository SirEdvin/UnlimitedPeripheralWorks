package site.siredvin.peripheralworks.integrations.modern_industrialization

import aztech.modern_industrialization.api.machine.holder.EnergyComponentHolder
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStorageLookup
import site.siredvin.peripheralworks.common.configuration.integration.modern_industrialization.Configuration
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy

class Integration : Runnable {
    override fun run() {
        AgnosticEnergyStorageLookup.addBlockLookup { _, _, entity, _ ->
            if (entity == null || !Configuration.enableEnergyStorage) return@addBlockLookup null
            if (entity is EnergyComponentHolder) {
                return@addBlockLookup MIEnergyStorage(entity.energyComponent)
            }
            return@addBlockLookup null
        }
        ComputerCraftProxy.addProvider(CraftingMachinePlugin.Provider)
    }
}
