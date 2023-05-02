package site.siredvin.peripheralworks.integrations.lifts

import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy

class Integration: Runnable {
    override fun run() {
        PeripheralWorksConfig.registerIntegrationConfiguration("lift", Configuration)
        ComputerCraftProxy.addProvider(LiftPlugin.Provider())
    }

}