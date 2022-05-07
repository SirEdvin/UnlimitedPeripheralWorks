package site.siredvin.peripheralworks.integrations.team_reborn_energy

import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy

class Integration: Runnable {
    override fun run() {
        ComputerCraftProxy.addProvider(EnergyStoragePlugin.Provider())
    }
}