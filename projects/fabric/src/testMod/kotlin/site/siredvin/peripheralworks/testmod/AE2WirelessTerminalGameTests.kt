package site.siredvin.peripheralworks.testmod

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEItemKey
import appeng.blockentity.networking.WirelessAccessPointBlockEntity
import appeng.blockentity.storage.ChestBlockEntity
import appeng.core.definitions.AEBlocks
import appeng.core.definitions.AEItems
import appeng.items.tools.powered.WirelessTerminalItem
import dan200.computercraft.api.turtle.TurtleSide
import dan200.computercraft.api.upgrades.UpgradeData
import dan200.computercraft.shared.config.Config
import dan200.computercraft.shared.turtle.blocks.TurtleBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.GlobalPos
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestAssertException
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import site.siredvin.peripheralworks.integrations.ae2.AE2CraftingMonitorUpgrade
import site.siredvin.peripheralworks.integrations.ae2.AE2WirelessTerminalUpgrade
import site.siredvin.testiarium.api.TestGroup
import site.siredvin.testiarium.api.thenExecuteFailFast
import site.siredvin.testiarium.cct.CctComputerState

@TestGroup("peripheralworks")
class AE2WirelessTerminalGameTests {
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

        val terminalItem = AEItems.WIRELESS_TERMINAL.asItem()
        val terminal = AEItems.WIRELESS_TERMINAL.stack().apply {
            hoverName = Component.literal("Test terminal")
            orCreateTag.putString("upw_test", "preserved")
        }
        WirelessTerminalItem.LINKABLE_HANDLER.link(terminal, GlobalPos.of(helper.level.dimension(), accessPointPos))
        terminalItem.injectAEPower(terminal, 400.0, Actionable.MODULATE)
        terminalItem.getUpgrades(terminal).setItemDirect(0, AEItems.ENERGY_CARD.stack())
        val initialCharge = terminalItem.getAECurrentPower(terminal)
        val upgrade = AE2WirelessTerminalUpgrade(AEItems.WIRELESS_TERMINAL.stack())
        check(ItemStack.matches(terminal, upgrade.getUpgradeItem(upgrade.getUpgradeData(terminal))))
        turtle.access.setUpgradeWithData(TurtleSide.LEFT, UpgradeData.of(upgrade, upgrade.getUpgradeData(terminal)))
        val craftingTerminal = AEItems.WIRELESS_CRAFTING_TERMINAL.stack()
        WirelessTerminalItem.LINKABLE_HANDLER.link(craftingTerminal, GlobalPos.of(helper.level.dimension(), accessPointPos))
        val craftingMonitor = AE2CraftingMonitorUpgrade(AEItems.WIRELESS_CRAFTING_TERMINAL.stack())
        check(ItemStack.matches(craftingTerminal, craftingMonitor.getUpgradeItem(craftingMonitor.getUpgradeData(craftingTerminal))))
        turtle.access.setUpgradeWithData(TurtleSide.RIGHT, UpgradeData.of(craftingMonitor, craftingMonitor.getUpgradeData(craftingTerminal)))

        helper.startSequence()
            .thenIdle(10)
            .thenExecuteFailFast {
                val accessPoint = helper.level.getBlockEntity(accessPointPos) as WirelessAccessPointBlockEntity
                check(accessPoint.isActive) { "Wireless access point did not become active" }
                val inserted = accessPoint.grid!!.storageService.inventory.insert(AEItemKey.of(Items.STONE), 64, Actionable.MODULATE, IActionSource.empty())
                check(inserted == 64L) { "Failed to seed AE2 item storage" }
                turtle.createServerComputer().turnOn()
            }
            .thenWaitUntil { await("initial") }
            .thenExecuteFailFast {
                state().check("initial")
                check(turtle.access.fuelLevel == 7) { "Expected three fuel-consuming calls, got ${turtle.access.fuelLevel}" }
                check(turtle.contents[0].count == 5 && turtle.contents[0].`is`(Items.STONE))
                check(turtle.contents[15].count == 5 && turtle.contents[15].`is`(Items.GOLD_INGOT))
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
                check(turtle.access.teleportTo(helper.level, accessPointPos.offset(0, 0, -1)))
            }
            .thenWaitUntil { await("returned") }
            .thenExecuteFailFast {
                state().check("returned")
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
            .thenExecuteFailFast {
                state().check(CctComputerState.DONE)
                check(helper.level.chunkSource.getChunkNow(UNLOADED_POS.x shr 4, UNLOADED_POS.z shr 4) == null) { "Wireless resolution loaded the linked chunk" }
                val stored = turtle.access.getUpgradeWithData(TurtleSide.LEFT)!!.upgradeItem
                check(stored.hoverName.string == "Test terminal")
                check(stored.tag?.getString("upw_test") == "preserved")
                check(terminalItem.getUpgrades(stored).getInstalledUpgrades(AEItems.ENERGY_CARD) == 1)
                check(terminalItem.getAECurrentPower(stored) == initialCharge) { "Peripheral use changed terminal charge" }
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

    companion object {
        private const val FIXTURE = "peripheralworksgametests.ae2_wireless_terminal"
        private val UNLOADED_POS = BlockPos(1_000_000, 64, 1_000_000)
    }
}
