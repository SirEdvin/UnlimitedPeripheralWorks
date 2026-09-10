@file:Suppress("DEPRECATION")

package site.siredvin.peripheralworks.integrations.ae2

// Inspired by Advanced Peripherals' WiredCableP2PTunnelPart by zyxkad:
// https://github.com/IntelligenceModding/AdvancedPeripherals/blob/fafb3877eed9c40b5a5d56b20421b8625b2d8cce/src/main/java/de/srendi/advancedperipherals/common/addons/ae2/WiredCableP2PTunnelPart.java

import appeng.api.networking.IGridNodeListener
import appeng.api.parts.IPartItem
import appeng.api.parts.IPartModel
import appeng.hooks.ticking.TickHandler
import appeng.items.parts.PartModels
import appeng.parts.p2p.P2PModels
import dan200.computercraft.api.ComputerCraftAPI
import dan200.computercraft.api.network.wired.WiredElement
import dan200.computercraft.api.network.wired.WiredNode
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import site.siredvin.peripheralworks.PeripheralWorksCore

class WiredNetworkP2PTunnelPart(partItem: IPartItem<*>) : WiredNetworkP2PTunnelBase(partItem) {
    companion object {
        private val MODELS = P2PModels(ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "part/p2p/wired_network_p2p_tunnel"))

        @JvmStatic
        @PartModels
        fun models(): List<IPartModel> = MODELS.models
    }

    private inner class Element : WiredElement {
        @JvmField
        val node: WiredNode = ComputerCraftAPI.createWiredNodeForElement(this)

        override fun getNode(): WiredNode = node
        override fun getLevel(): Level = this@WiredNetworkP2PTunnelPart.level
        override fun getPosition(): Vec3 = Vec3.atCenterOf(blockEntity.blockPos)
        override fun getSenderID(): String = "${PeripheralWorksCore.MOD_ID}:wired_network_p2p_tunnel"
    }

    private val internalElement = Element()
    private val outwardElement = Element()
    private val peers = mutableSetOf<WiredNetworkP2PTunnelPart>()
    private var externalNode: WiredNode? = null

    init {
        inputHandler = outwardElement
        outputHandler = outwardElement
        emptyHandler = null
    }

    override fun getStaticModels(): IPartModel = MODELS.getModel(isPowered, isActive)

    override fun onTunnelConfigChange() {
        super.onTunnelConfigChange()
        refreshConnections()
    }

    override fun onTunnelNetworkChange() {
        super.onTunnelNetworkChange()
        refreshConnections()
    }

    override fun onMainNodeStateChanged(reason: IGridNodeListener.State) {
        super.onMainNodeStateChanged(reason)
        refreshConnections()
    }

    override fun addToWorld() {
        super.addToWorld()
        refreshConnections()
    }

    override fun removeFromWorld() {
        disconnectAll(true)
        super.removeFromWorld()
    }

    override fun onNeighborChanged(level: BlockGetter, pos: BlockPos, neighbor: BlockPos) {
        super.onNeighborChanged(level, pos, neighbor)
        if (neighbor == facingPos) refreshExternalConnection()
    }

    private val facingPos: BlockPos
        get() = host.location.pos.relative(side)

    private fun refreshConnections() {
        if (isClientSide) return
        if (!isActive) {
            disconnectAll(false)
            return
        }
        if (!mainNode.hasGridBooted()) {
            TickHandler.instance().addCallable(level, ::refreshConnections)
            return
        }

        internalElement.node.connectTo(outwardElement.node)
        val linked = buildSet {
            getInput()?.takeIf { it !== this@WiredNetworkP2PTunnelPart && it.isActive }?.let(::add)
            getOutputStream().filter { it !== this@WiredNetworkP2PTunnelPart && it.isActive }.forEach(::add)
        }

        (peers - linked).toList().forEach(::disconnectPeer)
        (linked - peers).forEach {
            internalElement.node.connectTo(it.internalElement.node)
            peers.add(it)
            it.peers.add(this)
        }
        refreshExternalConnection()
    }

    private fun disconnectPeer(peer: WiredNetworkP2PTunnelPart) {
        internalElement.node.disconnectFrom(peer.internalElement.node)
        peers.remove(peer)
        peer.peers.remove(this)
    }

    private fun refreshExternalConnection() {
        if (isClientSide) return
        val found = if (isActive) findExternalWiredElement()?.node else null
        if (externalNode === found) return
        externalNode?.let(outwardElement.node::disconnectFrom)
        externalNode = found
        found?.let(outwardElement.node::connectTo)
    }

    private fun disconnectAll(removeNodes: Boolean) {
        externalNode?.let(outwardElement.node::disconnectFrom)
        externalNode = null
        peers.toList().forEach(::disconnectPeer)
        internalElement.node.disconnectFrom(outwardElement.node)
        if (removeNodes) {
            internalElement.node.remove()
            outwardElement.node.remove()
        }
    }
}
