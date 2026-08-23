package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.features.P2PTunnelAttunement
import appeng.core.definitions.AEBlockEntities
import dan200.computercraft.api.peripheral.PeripheralLookup
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy

object AE2Setup {
    fun registerPeripherals() {
        PeripheralLookup.get().registerForBlockEntity(
            { entity, side -> ComputerCraftProxy.peripheralProvider(entity.level!!, entity.blockPos, entity.blockState, entity, side) },
            AEBlockEntities.CABLE_BUS,
        )
    }

    fun registerAttunement() {
        ServerLifecycleEvents.SERVER_STARTING.register {
            P2PTunnelAttunement.registerAttunementTag(Registration.WIRED_NETWORK_P2P_TUNNEL.get())
        }
    }
}
