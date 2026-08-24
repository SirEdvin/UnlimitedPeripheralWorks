package site.siredvin.peripheralworks.testmod

import appeng.api.config.Actionable
import appeng.api.networking.IGrid
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEItemKey
import appeng.blockentity.networking.WirelessAccessPointBlockEntity
import appeng.blockentity.storage.ChestBlockEntity
import appeng.core.definitions.AEBlocks
import appeng.core.definitions.AEItems
import appeng.items.tools.powered.WirelessTerminalItem
import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.turtle.TurtleSide
import dan200.computercraft.api.upgrades.UpgradeData
import dan200.computercraft.shared.config.Config
import dan200.computercraft.shared.turtle.blocks.TurtleBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.GlobalPos
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestAssertException
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.phys.Vec3
import site.siredvin.peripheralworks.integrations.ae2.AE2WirelessTerminalPocketUpgrade
import site.siredvin.peripheralworks.integrations.ae2.AE2WirelessTerminalUpgrade
import site.siredvin.testiarium.api.TestGroup
import site.siredvin.testiarium.api.thenExecuteFailFast
import site.siredvin.testiarium.cct.CctComputerState
import site.siredvin.tweakium.modules.platform.ComputerPlatformToolkit

@TestGroup("ae2-configurable-peripherals")
class AE2WirelessTerminalGameTests {
    @Suppress("DEPRECATION")
    @GameTest(template = "empty")
    fun corruptPocketSubscriptionsAreDiscarded(helper: GameTestHelper) {
        val terminal = AEItems.WIRELESS_TERMINAL.stack()
        WirelessTerminalItem.LINKABLE_HANDLER.link(terminal, GlobalPos.of(helper.level.dimension(), helper.absolutePos(BlockPos(1, 1, 1))))
        val pocketData = ComputerPlatformToolkit.get().getPocketUpgrade(terminal)
            ?: error("Linked wireless terminal was not accepted as a pocket upgrade")
        pocketData.data.put("ae2StorageSubscriptions", malformedSubscriptions())
        var currentUpgrade: UpgradeData<IPocketUpgrade>? = pocketData
        var colour = -1
        var light = -1
        val access = object : IPocketAccess {
            override fun getLevel(): ServerLevel = helper.level
            override fun getPosition(): Vec3 = Vec3.atCenterOf(helper.absolutePos(BlockPos(1, 1, 1)))
            override fun getEntity(): Entity = helper.makeMockPlayer()
            override fun getColour(): Int = colour
            override fun setColour(value: Int) {
                colour = value
            }
            override fun getLight(): Int = light
            override fun setLight(value: Int) {
                light = value
            }
            override fun getUpgrade(): UpgradeData<IPocketUpgrade>? = currentUpgrade
            override fun setUpgrade(upgrade: UpgradeData<IPocketUpgrade>?) {
                currentUpgrade = upgrade
            }
            override fun getUpgradeNBTData(): CompoundTag = currentUpgrade!!.data
            override fun updateUpgradeNBTData() = Unit
            override fun invalidatePeripheral() = Unit

            @Suppress("OVERRIDE_DEPRECATION")
            override fun getUpgrades(): Map<ResourceLocation, IPeripheral> = emptyMap()
        }

        (pocketData.upgrade as AE2WirelessTerminalPocketUpgrade).createPeripheral(access)
        val definitions = pocketData.data.getCompound("ae2StorageSubscriptions").getList("subscriptions", net.minecraft.nbt.Tag.TAG_COMPOUND.toInt())
        check(definitions.isEmpty()) { "Corrupt pocket subscriptions were not discarded" }
        helper.succeed()
    }

    @GameTest(template = FIXTURE, batch = FIXTURE, timeoutTicks = 12000)
    fun wirelessTerminal(helper: GameTestHelper) {
        val turtle = findTurtle(helper)
        val accessPointPos = turtle.blockPos.offset(0, 0, 1)
        val energyPos = accessPointPos.offset(0, 0, 1)
        val chestPos = energyPos.offset(1, 0, 0)
        helper.level.setBlockAndUpdate(accessPointPos, AEBlocks.WIRELESS_ACCESS_POINT.block().defaultBlockState())
        helper.level.setBlockAndUpdate(energyPos, AEBlocks.CREATIVE_ENERGY_CELL.block().defaultBlockState())
        helper.level.setBlockAndUpdate(chestPos, AEBlocks.CHEST.block().defaultBlockState())
        (helper.level.getBlockEntity(chestPos) as ChestBlockEntity).setCell(AEItems.ITEM_CELL_1K.stack())

        val terminalItem = AEItems.WIRELESS_CRAFTING_TERMINAL.asItem()
        val terminal = AEItems.WIRELESS_CRAFTING_TERMINAL.stack().apply {
            hoverName = Component.literal("Test terminal")
            orCreateTag.putString("upw_test", "preserved")
        }
        WirelessTerminalItem.LINKABLE_HANDLER.link(terminal, GlobalPos.of(helper.level.dimension(), accessPointPos))
        terminalItem.injectAEPower(terminal, 400.0, Actionable.MODULATE)
        terminalItem.getUpgrades(terminal).setItemDirect(0, AEItems.ENERGY_CARD.stack())
        val initialCharge = terminalItem.getAECurrentPower(terminal)
        val standardTerminal = AEItems.WIRELESS_TERMINAL.stack()
        check(
            ComputerPlatformToolkit.get().getTurtleUpgrade(standardTerminal) == null &&
                ComputerPlatformToolkit.get().getTurtleUpgrade(AEItems.WIRELESS_CRAFTING_TERMINAL.stack()) == null &&
                ComputerPlatformToolkit.get().getPocketUpgrade(standardTerminal) == null &&
                ComputerPlatformToolkit.get().getPocketUpgrade(AEItems.WIRELESS_CRAFTING_TERMINAL.stack()) == null,
        ) {
            "Unlinked wireless terminal was accepted as a computer upgrade"
        }
        (standardTerminal.item as WirelessTerminalItem).let {
            WirelessTerminalItem.LINKABLE_HANDLER.link(standardTerminal, GlobalPos.of(helper.level.dimension(), accessPointPos))
            it.injectAEPower(standardTerminal, 100.0, Actionable.MODULATE)
        }
        check(ComputerPlatformToolkit.get().getTurtleUpgrade(standardTerminal)?.upgrade?.upgradeID == AE2WirelessTerminalUpgrade.UPGRADE_ID) {
            "Linked standard wireless terminal was not accepted as a turtle upgrade"
        }
        val standardPocketUpgrade = ComputerPlatformToolkit.get().getPocketUpgrade(standardTerminal)
        check(standardPocketUpgrade?.upgrade?.upgradeID == AE2WirelessTerminalUpgrade.UPGRADE_ID && ItemStack.matches(standardTerminal, standardPocketUpgrade.getUpgradeItem())) {
            "Linked standard wireless terminal was not accepted as a pocket upgrade"
        }
        val upgradeData = ComputerPlatformToolkit.get().getTurtleUpgrade(terminal)
            ?: error("Linked wireless terminal was not accepted as a turtle upgrade")
        check(upgradeData.upgrade.upgradeID == AE2WirelessTerminalUpgrade.CRAFTING_UPGRADE_ID) {
            "Linked wireless crafting terminal resolved to the wrong turtle upgrade"
        }
        val craftingPocketUpgrade = ComputerPlatformToolkit.get().getPocketUpgrade(terminal)
        check(craftingPocketUpgrade?.upgrade?.upgradeID == AE2WirelessTerminalUpgrade.CRAFTING_UPGRADE_ID && ItemStack.matches(terminal, craftingPocketUpgrade.getUpgradeItem())) {
            "Linked wireless crafting terminal resolved to the wrong pocket upgrade"
        }
        val upgrade = upgradeData.upgrade as AE2WirelessTerminalUpgrade
        check(ItemStack.matches(terminal, upgrade.getUpgradeItem(upgrade.getUpgradeData(terminal))))
        upgradeData.data.put("ae2StorageSubscriptions", malformedSubscriptions())
        turtle.access.setUpgradeWithData(TurtleSide.LEFT, upgradeData)
        lateinit var grid: IGrid
        var gridSizeWithoutObserver = 0
        var usedChannelsWithoutObserver = 0

        helper.startSequence()
            .thenIdle(10)
            .thenExecuteFailFast {
                val accessPoint = helper.level.getBlockEntity(accessPointPos) as WirelessAccessPointBlockEntity
                check(accessPoint.isActive) { "Wireless access point did not become active" }
                grid = accessPoint.grid!!
                val inserted = grid.storageService.inventory.insert(AEItemKey.of(Items.STONE), 64, Actionable.MODULATE, IActionSource.empty())
                check(inserted == 64L) { "Failed to seed AE2 item storage" }
                gridSizeWithoutObserver = grid.size()
                usedChannelsWithoutObserver = grid.pathingService.usedChannels
                turtle.createServerComputer().turnOn()
            }
            .thenWaitUntil { await("initial") }
            .thenExecuteFailFast {
                state().check("initial")
                check(turtle.access.fuelLevel == 7) { "Expected three fuel-consuming calls, got ${turtle.access.fuelLevel}" }
                check(turtle.contents[0].count == 5 && turtle.contents[0].`is`(Items.STONE))
                check(turtle.contents[15].count == 5 && turtle.contents[15].`is`(Items.GOLD_INGOT))
                check(grid.size() == gridSizeWithoutObserver + 1) { "Wireless storage observer was not attached" }
                check(grid.pathingService.usedChannels == usedChannelsWithoutObserver) { "Wireless storage observer consumed an AE2 channel" }
                turtle.access.fuelLevel = 0
            }
            .thenWaitUntil { await("empty-fuel") }
            .thenExecuteFailFast {
                state().check("empty-fuel")
                Config.turtlesNeedFuel = false
                turtle.access.fuelLevel = 1
            }
            .thenWaitUntil { await("disabled") }
            .thenExecuteFailFast {
                state().check("disabled")
                Config.turtlesNeedFuel = true
                turtle.access.fuelLevel = 2
            }
            .thenWaitUntil { await("restored") }
            .thenExecuteFailFast {
                state().check("restored")
                check(turtle.access.teleportTo(helper.level, turtle.blockPos.offset(18, 0, 0)))
            }
            .thenWaitUntil { await("out-of-range") }
            .thenExecuteFailFast {
                state().check("out-of-range")
                check(grid.size() == gridSizeWithoutObserver) { "Wireless storage observer was not removed outside range" }
                check(grid.storageService.inventory.insert(AEItemKey.of(Items.STONE), 1, Actionable.MODULATE, IActionSource.empty()) == 1L)
                check(turtle.access.teleportTo(helper.level, accessPointPos.offset(0, 0, -1)))
            }
            .thenWaitUntil { await("returned") }
            .thenExecuteFailFast {
                state().check("returned")
                check(grid.size() == gridSizeWithoutObserver + 1) { "Wireless storage observer was not restored" }
                check(grid.pathingService.usedChannels == usedChannelsWithoutObserver) { "Restored observer consumed an AE2 channel" }
                check(grid.storageService.inventory.insert(AEItemKey.of(Items.STONE), 1, Actionable.MODULATE, IActionSource.empty()) == 1L)
            }
            .thenWaitUntil { await("post-return-change") }
            .thenExecuteFailFast {
                state().check("post-return-change")
                helper.level.removeBlock(energyPos, false)
            }
            .thenWaitUntil { await("inactive") }
            .thenExecuteFailFast {
                state().check("inactive")
                helper.level.setBlockAndUpdate(energyPos, AEBlocks.CREATIVE_ENERGY_CELL.block().defaultBlockState())
            }
            .thenWaitUntil { await("reactivated") }
            .thenExecuteFailFast {
                state().check("reactivated")
                val data = turtle.access.getUpgradeNBTData(TurtleSide.LEFT)
                val stored = ItemStack.of(data.getCompound("terminal"))
                WirelessTerminalItem.LINKABLE_HANDLER.link(stored, GlobalPos.of(helper.level.dimension(), UNLOADED_POS))
                data.put("terminal", stored.save(net.minecraft.nbt.CompoundTag()))
                turtle.access.updateUpgradeNBTData(TurtleSide.LEFT)
                check(helper.level.chunkSource.getChunkNow(UNLOADED_POS.x shr 4, UNLOADED_POS.z shr 4) == null)
            }
            .thenWaitUntil { await(CctComputerState.DONE) }
            .thenIdle(2)
            .thenExecuteFailFast {
                state().check(CctComputerState.DONE)
                check(helper.level.chunkSource.getChunkNow(UNLOADED_POS.x shr 4, UNLOADED_POS.z shr 4) == null) { "Wireless resolution loaded the linked chunk" }
                val stored = turtle.access.getUpgradeWithData(TurtleSide.LEFT)!!.upgradeItem
                check(stored.hoverName.string == "Test terminal")
                check(stored.tag?.getString("upw_test") == "preserved")
                check(terminalItem.getUpgrades(stored).getInstalledUpgrades(AEItems.ENERGY_CARD) == 1)
                check(terminalItem.getAECurrentPower(stored) == initialCharge) { "Peripheral use changed terminal charge" }
                val currentGrid = (helper.level.getBlockEntity(accessPointPos) as WirelessAccessPointBlockEntity).grid!!
                check(currentGrid.size() == gridSizeWithoutObserver) { "Wireless storage observer was not cleaned up" }
                val definitions = turtle.access.getUpgradeNBTData(TurtleSide.LEFT).getCompound("ae2StorageSubscriptions").getList("subscriptions", net.minecraft.nbt.Tag.TAG_COMPOUND.toInt())
                check(definitions.size == 1 && definitions.getCompound(0).getString("name") == "stone") { "Corrupt turtle subscriptions were not replaced with valid persisted data" }
            }
            .thenSucceed()
    }

    private fun state() = CctComputerState.get(FIXTURE) ?: throw GameTestAssertException("Computer '$FIXTURE' has not started")

    private fun findTurtle(helper: GameTestHelper): TurtleBlockEntity {
        for (x in 0 until 7) {
            for (y in 0 until 4) {
                for (z in 0 until 7) {
                    val entity = helper.getBlockEntity(BlockPos(x, y, z))
                    if (entity is TurtleBlockEntity) return entity
                }
            }
        }
        throw GameTestAssertException("Fixture turtle is missing")
    }

    private fun await(marker: String) {
        val state = state()
        if (state.isDone(CctComputerState.DONE)) state.check(CctComputerState.DONE)
        if (!state.isDone(marker)) throw GameTestAssertException("Computer '$FIXTURE' has not reached $marker")
    }

    private fun malformedSubscriptions(): CompoundTag = CompoundTag().apply {
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
    }

    companion object {
        private const val FIXTURE = "peripheralworksgametests.ae2_wireless_terminal"
        private val UNLOADED_POS = BlockPos(1_000_000, 64, 1_000_000)
    }
}
