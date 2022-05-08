package site.siredvin.peripheralworks.integrations.team_reborn_energy

import dan200.computercraft.api.turtle.event.TurtleEvent
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy

class Integration: Runnable {
    override fun run() {
        ComputerCraftProxy.addProvider(EnergyStoragePlugin.Provider())
        TurtleEvent.EVENT_BUS.register(EnergyRefuelHandler)
    }
}