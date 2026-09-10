@file:Suppress("DEPRECATION")

package site.siredvin.peripheralworks.testmod

import appeng.api.config.Actionable
import appeng.api.features.P2PTunnelAttunement
import appeng.api.networking.GridFlags
import appeng.api.networking.security.IActionSource
import appeng.api.orientation.BlockOrientation
import appeng.api.parts.PartHelper
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.KeyCounter
import appeng.api.storage.StorageCells
import appeng.api.util.AECableType
import appeng.api.util.AEColor
import appeng.blockentity.storage.DriveBlockEntity
import appeng.core.definitions.AEBlocks
import appeng.core.definitions.AEItems
import appeng.core.definitions.AEParts
import appeng.parts.p2p.P2PTunnelPart
import dan200.computercraft.api.network.Packet
import dan200.computercraft.api.network.PacketReceiver
import dan200.computercraft.shared.ModRegistry
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.Vec3
import site.siredvin.broccolium.modules.storage.fluid.AgnosticFluidStack
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.integrations.ae2.AE2StorageSubscriptionPlugin
import site.siredvin.peripheralworks.integrations.ae2.AE2StorageSubscriptionPluginProvider
import site.siredvin.peripheralworks.integrations.ae2.AE2StorageSubscriptionTracker
import site.siredvin.peripheralworks.integrations.ae2.AEFluidKeyFactory
import site.siredvin.peripheralworks.integrations.ae2.Integration
import site.siredvin.peripheralworks.integrations.ae2.MENetworkBlockPlugin
import site.siredvin.peripheralworks.integrations.ae2.MENetworkPeripheralBlockEntity
import site.siredvin.peripheralworks.integrations.ae2.Registration
import site.siredvin.testiarium.api.TestGroup
import site.siredvin.testiarium.cct.thenLua
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.concurrent.atomic.AtomicReference
import site.siredvin.peripheralworks.common.setup.Blocks as ModBlocks
import site.siredvin.peripheralworks.common.setup.Items as ModItems

private val dan200.computercraft.api.network.wired.WiredNode.network
    get() = (this as dan200.computercraft.impl.network.wired.WiredNodeImpl).network

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
                check(MENetworkBlockPlugin.Provider.provide(helper.level, helper.absolutePos(interfacePos), Direction.UP) != null)
                check(AE2StorageSubscriptionPluginProvider.provide(helper.level, helper.absolutePos(peripheralPos), Direction.UP) != null)
                check(AE2StorageSubscriptionPluginProvider.provide(helper.level, helper.absolutePos(interfacePos), Direction.UP) == null)
                check(ComputerCraftProxy.peripheralProvider(helper.level, helper.absolutePos(peripheralPos), peripheral.blockState, peripheral, Direction.UP)!!.additionalTypes.contains("ae2_network_access"))
                check(!ComputerCraftProxy.peripheralProvider(helper.level, helper.absolutePos(interfacePos), helper.getBlockState(interfacePos), helper.getBlockEntity(interfacePos), Direction.UP)!!.additionalTypes.contains("ae2_network_access"))
                check(Integration.extractItemStorage(helper.level, helper.absolutePos(interfacePos), helper.getBlockEntity(interfacePos), Direction.UP) != null)
                check(peripheral.mainNode.isActive)
                check(MENetworkBlockPlugin(peripheral).getChannelInformation().isNotEmpty())
                helper.destroyBlock(peripheralPos)
            }
            .thenIdle(2)
            .thenExecute {
                check(helper.level.getBlockEntity(helper.absolutePos(peripheralPos)) == null)
            }
            .thenSucceed()
    }

    @GameTest(template = "empty")
    fun storageSubscriptionPersistenceAndCorruption(helper: GameTestHelper) {
        val pos = BlockPos(1, 1, 1)
        helper.setBlock(pos, Registration.ME_NETWORK_PERIPHERAL.get())
        val entity = helper.getBlockEntity(pos) as MENetworkPeripheralBlockEntity
        val plugin = AE2StorageSubscriptionPlugin(entity.subscriptionTracker)
        plugin.subscribe("items", "item", mapOf("all" to mapOf(1 to mapOf("name" to "minecraft:diamond"))))
        plugin.subscribe("water", "fluid", "minecraft:water")

        val saved = CompoundTag()
        entity.saveAdditional(saved, helper.level.registryAccess())
        val restored = MENetworkPeripheralBlockEntity(helper.absolutePos(pos), entity.blockState)
        restored.loadTag(saved, helper.level.registryAccess())
        val restoredSubscriptions = AE2StorageSubscriptionPlugin(restored.subscriptionTracker).getSubscriptions()
        check(restoredSubscriptions.map { it["name"] } == listOf("items", "water"))
        check(restoredSubscriptions.first()["filter"] is Map<*, *>)

        val corrupt = CompoundTag().apply {
            put(
                "ae2StorageSubscriptions",
                CompoundTag().apply {
                    put(
                        "subscriptions",
                        ListTag().apply {
                            add(
                                CompoundTag().apply {
                                    putString("name", "broken")
                                    putString("subscriptionType", "item")
                                    put("filter", CompoundTag().apply { putByte("type", 99) })
                                },
                            )
                        },
                    )
                },
            )
        }
        restored.loadTag(corrupt, helper.level.registryAccess())
        check(AE2StorageSubscriptionPlugin(restored.subscriptionTracker).getSubscriptions().isEmpty())
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun storageSubscriptionLimits(helper: GameTestHelper) {
        val tracker = AE2StorageSubscriptionTracker(maxSubscriptions = 2, maxItemFilterSize = 3)
        val plugin = AE2StorageSubscriptionPlugin(tracker)
        plugin.subscribe("first", "item", "minecraft:diamond")
        plugin.subscribe("second", "fluid", "minecraft:water")
        plugin.subscribe("first", "item", "minecraft:stone")
        check(runCatching { plugin.subscribe("third", "fluid", null) }.exceptionOrNull() is dan200.computercraft.api.lua.LuaException)
        check(plugin.getSubscriptions().map { it["name"] } == listOf("first", "second"))
        check(
            runCatching {
                plugin.subscribe("first", "item", mapOf("all" to mapOf(1 to mapOf("name" to "minecraft:diamond"))))
            }.exceptionOrNull() is dan200.computercraft.api.lua.LuaException,
        )
        check(plugin.getSubscriptions().first { it["name"] == "first" }["filter"] == "minecraft:stone")

        val persisted = AE2StorageSubscriptionTracker(maxSubscriptions = 3).apply {
            subscribe("a", "fluid", null)
            subscribe("b", "fluid", null)
            subscribe("c", "fluid", null)
        }.save()
        val restored = AE2StorageSubscriptionTracker(maxSubscriptions = 2)
        check(restored.load(persisted))
        check(restored.getSubscriptions().map { it["name"] } == listOf("a", "b"))
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun storageSubscriptionWireLimitsAndAtomicity(helper: GameTestHelper) {
        fun roundTrip(tracker: AE2StorageSubscriptionTracker, maxValues: Int = 1024) {
            val bytes = ByteArrayOutputStream()
            DataOutputStream(bytes).use { NbtIo.write(tracker.save(), it) }
            val restored = AE2StorageSubscriptionTracker(maxItemFilterSize = maxValues)
            check(!restored.load(DataInputStream(ByteArrayInputStream(bytes.toByteArray())).use { NbtIo.read(it)!! }))
            check(restored.getSubscriptions() == tracker.getSubscriptions()) { "Definitions did not survive the NBT wire format" }
        }
        val limited = AE2StorageSubscriptionTracker(maxItemFilterSize = 3)
        limited.subscribe("existing", "item", mapOf("displayName" to "Original"))
        var persisted = limited.save()
        limited.onDefinitionsChanged = { persisted = limited.save() }
        val original = limited.getSubscriptions()
        val originalTag = persisted.copy()
        check(runCatching { limited.subscribe("existing", "item", mapOf("displayName" to "Stone", "tag" to "minecraft:logs")) }.exceptionOrNull() is dan200.computercraft.api.lua.LuaException)
        check(limited.getSubscriptions() == original && persisted == originalTag)
        roundTrip(limited, 3)
        limited.onDefinitionsChanged = { error("Persistence unavailable") }
        check(runCatching { limited.subscribe("existing", "item", "minecraft:stone") }.isFailure)
        check(limited.getSubscriptions() == original)
        check(runCatching { limited.subscribe("new", "fluid", null) }.isFailure)
        check(limited.getSubscriptions() == original)

        val tracker = AE2StorageSubscriptionTracker(maxItemFilterSize = 1024)
        tracker.subscribe("tags", "item", mapOf("tag" to mapOf("in" to (1..509).associateWith { "example:tag$it" })))
        roundTrip(tracker)
        val before = tracker.getSubscriptions()
        check(runCatching { tracker.subscribe("tags", "item", mapOf("tag" to mapOf("in" to (1..510).associateWith { "example:tag$it" }))) }.exceptionOrNull() is dan200.computercraft.api.lua.LuaException)
        check(tracker.getSubscriptions() == before)
        roundTrip(tracker)
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun storageSubscriptionStringBounds(helper: GameTestHelper) {
        val tracker = AE2StorageSubscriptionTracker()
        listOf("a".repeat(65535), "\u0000".repeat(32767) + "a", "界".repeat(21845)).forEach { tracker.subscribe(it, "fluid", null) }
        tracker.subscribe("large-filter", "item", mapOf("displayName" to "a".repeat(65000)))
        val before = tracker.getSubscriptions()
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { NbtIo.write(tracker.save(), it) }
        val restored = AE2StorageSubscriptionTracker()
        check(!restored.load(DataInputStream(ByteArrayInputStream(bytes.toByteArray())).use { NbtIo.read(it)!! }))
        check(restored.getSubscriptions() == before)
        listOf("a".repeat(65536), "\u0000".repeat(32768), "界".repeat(21846), "🌳".repeat(10923)).forEach { name ->
            check(runCatching { tracker.subscribe(name, "fluid", null) }.exceptionOrNull() is dan200.computercraft.api.lua.LuaException)
        }
        listOf(
            mapOf("displayName" to "a".repeat(65536)),
            mapOf("displayName" to "界".repeat(21846)),
            mapOf("displayName" to "\u0000".repeat(32768)),
            mapOf("a".repeat(65536) to "value"),
            mapOf("displayName" to "a".repeat(33000), "tag" to "b".repeat(33000)),
        ).forEach { filter ->
            check(runCatching { tracker.subscribe("large-filter", "item", filter) }.exceptionOrNull() is dan200.computercraft.api.lua.LuaException)
        }
        check(tracker.getSubscriptions() == before) { "Rejected strings changed definitions" }
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun storageSubscriptionConcurrentAttachment(helper: GameTestHelper) {
        val tracker = AE2StorageSubscriptionTracker()
        tracker.subscribe("stone", "item", "minecraft:stone")
        val key = AEItemKey.of(Items.STONE)
        tracker.baseline(KeyCounter())
        var oldSinkEvents = 0
        var newSinkEvents = 0
        val oldSink: (String, Map<String, Any>, Long, Long) -> Unit = { _, _, _, _ -> oldSinkEvents++ }
        val newSink: (String, Map<String, Any>, Long, Long) -> Unit = { _, _, _, _ -> newSinkEvents++ }
        val failure = AtomicReference<Throwable>()
        tracker.addEventSink { _, _, _, _ ->
            val attachment = Thread {
                try {
                    tracker.removeEventSink(oldSink)
                    tracker.addEventSink(newSink)
                } catch (error: Throwable) {
                    failure.set(error)
                }
            }
            attachment.start()
            attachment.join(1000)
            check(!attachment.isAlive) { "Attach/detach blocked on event dispatch" }
            failure.get()?.let { throw it }
        }
        tracker.addEventSink(oldSink)
        tracker.onStackChange(key, 1)
        check(oldSinkEvents == 1 && newSinkEvents == 0) { "In-flight dispatch did not retain its snapshot" }
        tracker.onStackChange(key, 2)
        check(oldSinkEvents == 1 && newSinkEvents == 1) { "Subsequent dispatch did not see changed attachments" }
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun disconnectedCallsAndAttunementAreSafe(helper: GameTestHelper) {
        val pos = BlockPos(1, 1, 1)
        helper.setBlock(pos, Registration.ME_NETWORK_PERIPHERAL.get())
        val peripheral = helper.getBlockEntity(pos) as MENetworkPeripheralBlockEntity
        val plugin = MENetworkBlockPlugin(peripheral)
        check(plugin.getAverageEnergyDemand() == 0.0)
        check(plugin.getChannelInformation().isEmpty())
        check(P2PTunnelAttunement.getTunnelPartByTriggerItem(ModRegistry.Items.CABLE.get().defaultInstance).item != Registration.WIRED_NETWORK_P2P_TUNNEL.get())
        check(P2PTunnelAttunement.getTunnelPartByTriggerItem(ModRegistry.Items.WIRED_MODEM.get().defaultInstance).item != Registration.WIRED_NETWORK_P2P_TUNNEL.get())
        check(P2PTunnelAttunement.getTunnelPartByTriggerItem(ModRegistry.Items.WIRED_MODEM_FULL.get().defaultInstance).item != Registration.WIRED_NETWORK_P2P_TUNNEL.get())
        check(P2PTunnelAttunement.getTunnelPartByTriggerItem(ModBlocks.NETWORK_MANAGER.get().asItem().defaultInstance).item != Registration.WIRED_NETWORK_P2P_TUNNEL.get())
        check(P2PTunnelAttunement.getTunnelPartByTriggerItem(ModItems.ULTIMATE_CONFIGURATOR.get().defaultInstance).item == Registration.WIRED_NETWORK_P2P_TUNNEL.get())
        helper.succeed()
    }

    @GameTest(template = "ae2gametests.item_and_fluid_storage_transfers", timeoutTicks = 2400)
    fun itemAndFluidStorageTransfers(helper: GameTestHelper) {
        helper.setBlock(BlockPos(3, 2, 4), Registration.ME_NETWORK_PERIPHERAL.get())
        helper.setBlock(BlockPos(3, 2, 5), AEBlocks.DRIVE.block())
        helper.setBlock(BlockPos(2, 2, 4), AEBlocks.CREATIVE_ENERGY_CELL.block())
        helper.setBlock(BlockPos(3, 2, 2), Registration.ME_NETWORK_PERIPHERAL.get())
        helper.setBlock(BlockPos(3, 2, 1), AEBlocks.DRIVE.block())
        helper.setBlock(BlockPos(2, 2, 2), AEBlocks.CREATIVE_ENERGY_CELL.block())
        val drive = helper.getBlockEntity(BlockPos(3, 2, 5)) as DriveBlockEntity
        val secondaryDrive = helper.getBlockEntity(BlockPos(3, 2, 1)) as DriveBlockEntity
        BlockOrientation.EAST_UP.setOn(drive)
        BlockOrientation.EAST_UP.setOn(secondaryDrive)
        val itemCell = AEItems.ITEM_CELL_1K.stack()
        val fluidCell = AEItems.FLUID_CELL_1K.stack()
        val source = IActionSource.ofMachine(drive)
        val itemInventory = StorageCells.getCellInventory(itemCell, null)!!
        check(itemInventory.insert(AEItemKey.of(ItemStack(Items.DIAMOND)), 8, Actionable.MODULATE, source) == 8L)
        itemInventory.persist()
        val water = AgnosticFluidStack(Fluids.WATER, 1_000.0)
        val fluidInventory = StorageCells.getCellInventory(fluidCell, null)!!
        check(fluidInventory.insert(AEFluidKeyFactory.of(water), water.platformAmount.toLong(), Actionable.MODULATE, source) > 0)
        fluidInventory.persist()
        check(drive.internalInventory.insertItem(0, itemCell, false).isEmpty)
        check(drive.internalInventory.insertItem(1, fluidCell, false).isEmpty)
        check(secondaryDrive.internalInventory.insertItem(0, AEItems.ITEM_CELL_1K.stack(), false).isEmpty)
        check(secondaryDrive.internalInventory.insertItem(1, AEItems.FLUID_CELL_1K.stack(), false).isEmpty)

        helper.thenLua().thenSucceed()
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    fun wiredP2POneToManyAndCleanup(helper: GameTestHelper) {
        val hosts = listOf(BlockPos(2, 2, 2), BlockPos(4, 2, 2), BlockPos(6, 2, 2))
        val player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL)
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
                hosts.forEachIndexed { index, pos ->
                    check(AE2TestWiredLookup.find(helper.level, helper.absolutePos(pos), Direction.NORTH) === parts[index].exposedApi) {
                        "P2P part did not expose its wired capability on the outward side"
                    }
                    check(AE2TestWiredLookup.find(helper.level, helper.absolutePos(pos), Direction.SOUTH) == null) {
                        "P2P part exposed its wired capability on a non-outward side"
                    }
                }
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
