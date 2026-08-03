package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.features.P2PTunnelAttunement
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents

object AE2Setup {
    fun registerAttunement() {
        ServerLifecycleEvents.SERVER_STARTING.register {
            P2PTunnelAttunement.registerAttunementTag(Registration.WIRED_NETWORK_P2P_TUNNEL.get())
        }
    }
}
