package site.siredvin.peripheralworks.integrations.ae2

// Fabric adaptation of Advanced Peripherals' WiredCableP2PTunnelPart by zyxkad:
// https://github.com/IntelligenceModding/AdvancedPeripherals/blob/fafb3877eed9c40b5a5d56b20421b8625b2d8cce/src/main/java/de/srendi/advancedperipherals/common/addons/ae2/WiredCableP2PTunnelPart.java

import appeng.api.parts.IPartItem
import appeng.parts.p2p.CapabilityP2PTunnelPart
import dan200.computercraft.api.network.wired.WiredElement
import dan200.computercraft.api.node.wired.WiredElementLookup

abstract class WiredNetworkP2PTunnelBase(partItem: IPartItem<*>) : CapabilityP2PTunnelPart<WiredNetworkP2PTunnelPart, WiredElement>(partItem, WiredElementLookup.get()) {

    protected fun findExternalWiredElement(): WiredElement? = WiredElementLookup.get().find(level, host.location.pos.relative(side), side.opposite)
}
