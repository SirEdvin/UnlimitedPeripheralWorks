package site.siredvin.peripheralworks.integrations.ae2

import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy

class Integration : Runnable {
    override fun run() {
        PeripheralWorksConfig.registerIntegrationConfiguration("ae2", Configuration)
        ComputerCraftProxy.addProvider(MENetworkBlockPlugin.Provider())
    }
}
