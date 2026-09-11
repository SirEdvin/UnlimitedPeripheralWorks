package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.AECapabilities
import appeng.api.features.P2PTunnelAttunement
import appeng.api.parts.RegisterPartCapabilitiesEvent
import dan200.computercraft.api.network.wired.WiredElementCapability
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent

object AE2Setup {
    fun registerCapabilities(event: RegisterCapabilitiesEvent) {
        event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, Registration.ME_NETWORK_PERIPHERAL_BLOCK_ENTITY.get()) { entity, _ -> entity }
    }

    fun registerPartCapabilities(event: RegisterPartCapabilitiesEvent) {
        event.register(WiredElementCapability.get(), { part, _ -> part.exposedApi }, WiredNetworkP2PTunnelPart::class.java)
    }

    fun registerPeripherals() = Unit

    fun registerAttunement() {
        P2PTunnelAttunement.registerAttunementTag(Registration.WIRED_NETWORK_P2P_TUNNEL.get())
    }
}
