package site.siredvin.peripheralworks.integrations.modern_industrialization

import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy

class Integration : Runnable {
    override fun run() {
        ComputerCraftProxy.addProvider(EnergyStoragePlugin.Provider())
        ComputerCraftProxy.addProvider(CraftingMachinePlugin.Provider())
        PeripheralWorksConfig.registerIntegrationConfiguration("modern_industrialization", Configuration)
    }
}
