package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.features.P2PTunnelAttunement

object AE2Setup {
    fun registerPeripherals() = Unit

    fun registerAttunement() {
        P2PTunnelAttunement.registerAttunementTag(Registration.WIRED_NETWORK_P2P_TUNNEL.get())
    }
}
