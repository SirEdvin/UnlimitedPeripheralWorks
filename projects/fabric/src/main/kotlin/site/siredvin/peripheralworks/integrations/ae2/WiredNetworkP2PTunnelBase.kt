package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.parts.IPartItem
import appeng.parts.p2p.CapabilityP2PTunnelPart
import dan200.computercraft.api.network.wired.WiredElement
import dan200.computercraft.api.node.wired.WiredElementLookup

abstract class WiredNetworkP2PTunnelBase(partItem: IPartItem<*>) : CapabilityP2PTunnelPart<WiredNetworkP2PTunnelPart, WiredElement>(partItem, WiredElementLookup.get()) {

    protected fun findExternalWiredElement(): WiredElement? = WiredElementLookup.get().find(level, host.location.pos.relative(side), side.opposite)
}
