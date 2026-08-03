@file:Suppress("DEPRECATION")

package site.siredvin.peripheralworks.testmod

import appeng.api.features.P2PTunnelAttunement
import appeng.api.networking.GridFlags
import appeng.api.parts.PartHelper
import appeng.api.util.AECableType
import appeng.api.util.AEColor
import appeng.core.definitions.AEBlocks
import appeng.core.definitions.AEParts
import appeng.parts.p2p.P2PTunnelPart
import dan200.computercraft.api.network.Packet
import dan200.computercraft.api.network.PacketReceiver
import dan200.computercraft.shared.ModRegistry
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.phys.Vec3
import site.siredvin.peripheralworks.integrations.ae2.Integration
import site.siredvin.peripheralworks.integrations.ae2.MENetworkBlockPlugin
import site.siredvin.peripheralworks.integrations.ae2.MENetworkPeripheralBlockEntity
import site.siredvin.peripheralworks.integrations.ae2.Registration
import site.siredvin.testiarium.api.TestGroup

@TestGroup("ae2")
class AE2GameTests {
    @GameTest(template = "empty", timeoutTicks = 100)
    fun meNetworkPeripheralScopeAndLifecycle(helper: GameTestHelper) {
        val energyPos = BlockPos(1, 1, 1)
        val peripheralPos = BlockPos(2, 1, 1)
        val interfacePos = BlockPos(1, 1, 2)
        helper.setBlock(energyPos, AEBlocks.CREATIVE_ENERGY_CELL.block())
        helper.setBlock(peripheralPos, Registration.ME_NETWORK_PERIPHERAL.get())
        helper.setBlock(interfacePos, AEBlocks.INTERFACE.block())

        helper.startSequence()
            .thenIdle(10)
            .thenExecute {
                val peripheral = helper.getBlockEntity(peripheralPos) as MENetworkPeripheralBlockEntity
                check(peripheral.mainNode.node!!.hasFlag(GridFlags.REQUIRE_CHANNEL))
                Direction.entries.forEach { check(peripheral.getCableConnectionType(it) == AECableType.SMART) }
                check(MENetworkBlockPlugin.Provider.provide(helper.level, helper.absolutePos(peripheralPos), Direction.UP) != null)
                check(MENetworkBlockPlugin.Provider.provide(helper.level, helper.absolutePos(interfacePos), Direction.UP) == null)
                check(Integration.extractItemStorage(helper.level, helper.absolutePos(interfacePos), helper.getBlockEntity(interfacePos), Direction.UP) != null)
                check(peripheral.mainNode.isActive)
                check(MENetworkBlockPlugin(helper.level, peripheral).getChannelInformation().isNotEmpty())
                helper.destroyBlock(peripheralPos)
            }
            .thenIdle(2)
            .thenExecute {
                check(helper.getBlockEntity(peripheralPos) == null)
            }
            .thenSucceed()
    }

    @GameTest(template = "empty")
    fun disconnectedCallsAndAttunementAreSafe(helper: GameTestHelper) {
        val pos = BlockPos(1, 1, 1)
        helper.setBlock(pos, Registration.ME_NETWORK_PERIPHERAL.get())
        val peripheral = helper.getBlockEntity(pos) as MENetworkPeripheralBlockEntity
        val plugin = MENetworkBlockPlugin(helper.level, peripheral)
        check(plugin.getAverageEnergyDemand() == 0.0)
        check(plugin.getChannelInformation().isEmpty())
        check(P2PTunnelAttunement.getTunnelPartByTriggerItem(ModRegistry.Items.CABLE.get().defaultInstance).item == Registration.WIRED_NETWORK_P2P_TUNNEL.get())
        check(P2PTunnelAttunement.getTunnelPartByTriggerItem(ModRegistry.Items.WIRED_MODEM.get().defaultInstance).item == Registration.WIRED_NETWORK_P2P_TUNNEL.get())
        check(P2PTunnelAttunement.getTunnelPartByTriggerItem(ModRegistry.Items.WIRED_MODEM_FULL.get().defaultInstance).item == Registration.WIRED_NETWORK_P2P_TUNNEL.get())
        helper.succeed()
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    fun wiredP2POneToManyAndCleanup(helper: GameTestHelper) {
        val hosts = listOf(BlockPos(2, 2, 2), BlockPos(4, 2, 2), BlockPos(6, 2, 2))
        val player = helper.makeMockPlayer()
        helper.setBlock(BlockPos(1, 2, 2), AEBlocks.CREATIVE_ENERGY_CELL.block())
        (2..6).forEach { x ->
            PartHelper.setPart(helper.level, helper.absolutePos(BlockPos(x, 2, 2)), null, player, AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT))
        }
        val parts = hosts.map { pos ->
            PartHelper.setPart(
                helper.level,
                helper.absolutePos(pos),
                Direction.NORTH,
                player,
                Registration.WIRED_NETWORK_P2P_TUNNEL.get(),
            )!!.also { it.frequency = 42 }
        }
        // ponytail: AE2 intentionally exposes output selection only through memory-card interaction; reflection keeps this topology test fixture-free.
        val setOutput = P2PTunnelPart::class.java.getDeclaredMethod("setOutput", Boolean::class.javaPrimitiveType).apply { isAccessible = true }
        setOutput.invoke(parts[1], true)
        setOutput.invoke(parts[2], true)
        hosts.forEach { helper.setBlock(it.relative(Direction.NORTH), ModRegistry.Blocks.WIRED_MODEM_FULL.get()) }
        val isolatedPos = hosts[0].relative(Direction.SOUTH)
        helper.setBlock(isolatedPos, ModRegistry.Blocks.WIRED_MODEM_FULL.get())

        helper.startSequence()
            .thenIdle(20)
            .thenExecute {
                val elements = hosts.map { AE2TestWiredLookup.find(helper.level, helper.absolutePos(it.relative(Direction.NORTH)), null)!! }
                check(elements.map { it.node.network }.distinct().size == 1) { "One-to-many endpoint networks were not joined" }
                val isolated = AE2TestWiredLookup.find(helper.level, helper.absolutePos(isolatedPos), null)!!
                check(elements[0].node.network !== isolated.node.network) { "A non-outward neighbor joined the tunnel" }

                var received = false
                elements[2].node.addReceiver(object : PacketReceiver {
                    override fun getLevel() = helper.level
                    override fun getPosition() = Vec3.atCenterOf(helper.absolutePos(hosts[2]))
                    override fun getRange() = Double.MAX_VALUE
                    override fun isInterdimensional() = true
                    override fun receiveSameDimension(packet: Packet, distance: Double) {
                        received = true
                    }
                    override fun receiveDifferentDimension(packet: Packet) {
                        received = true
                    }
                })
                elements[0].node.transmitSameDimension(Packet(1, 2, "test", elements[0]), Double.MAX_VALUE)
                check(received) { "Packet did not cross the P2P bridge" }
                setOutput.invoke(parts[2], false)
                parts[2].frequency = 43
                parts[2].onTunnelConfigChange()
            }
            .thenIdle(5)
            .thenExecute {
                val elements = hosts.map { AE2TestWiredLookup.find(helper.level, helper.absolutePos(it.relative(Direction.NORTH)), null)!! }
                check(elements[0].node.network === elements[1].node.network)
                check(elements[0].node.network !== elements[2].node.network) { "Relink left a stale wired edge" }
                PartHelper.getPartHost(helper.level, helper.absolutePos(hosts[1]))!!.removePartFromSide(Direction.NORTH)
            }
            .thenIdle(5)
            .thenExecute {
                val first = AE2TestWiredLookup.find(helper.level, helper.absolutePos(hosts[0].relative(Direction.NORTH)), null)!!
                val removed = AE2TestWiredLookup.find(helper.level, helper.absolutePos(hosts[1].relative(Direction.NORTH)), null)!!
                check(first.node.network !== removed.node.network) { "Part removal left a stale wired edge" }
                helper.destroyBlock(BlockPos(1, 2, 2))
            }
            .thenIdle(5)
            .thenExecute {
                val elements = hosts.map { AE2TestWiredLookup.find(helper.level, helper.absolutePos(it.relative(Direction.NORTH)), null)!! }
                check(elements.map { it.node.network }.distinct().size == 3) { "Inactive endpoints retained wired edges" }
            }
            .thenSucceed()
    }
}
