package site.siredvin.peripheralworks.integrations.lifts

import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.integrations.team_reborn_energy.Configuration

class Integration: Runnable {
    override fun run() {
        PeripheralWorksConfig.registerIntegrationConfiguration("lift", Configuration)
        ComputerCraftProxy.addProvider(LiftPlugin.Provider())
    }

}