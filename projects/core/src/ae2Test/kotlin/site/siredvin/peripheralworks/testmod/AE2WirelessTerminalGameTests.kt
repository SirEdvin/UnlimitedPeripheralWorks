package site.siredvin.peripheralworks.testmod

import appeng.api.config.Actionable
import appeng.api.crafting.PatternDetailsHelper
import appeng.api.networking.IGrid
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.GenericStack
import appeng.blockentity.crafting.PatternProviderBlockEntity
import appeng.blockentity.networking.WirelessAccessPointBlockEntity
import appeng.blockentity.storage.MEChestBlockEntity
import appeng.core.definitions.AEBlocks
import appeng.core.definitions.AEItems
import appeng.items.tools.powered.WirelessTerminalItem
import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.turtle.TurtleSide
import dan200.computercraft.api.upgrades.UpgradeData
import dan200.computercraft.impl.PocketUpgrades
import dan200.computercraft.impl.TurtleUpgrades
import dan200.computercraft.shared.config.Config
import dan200.computercraft.shared.turtle.blocks.TurtleBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.GlobalPos
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestAssertException
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.phys.Vec3
import site.siredvin.peripheralworks.integrations.ae2.AE2WirelessTerminalPocketUpgrade
import site.siredvin.peripheralworks.integrations.ae2.AE2WirelessTerminalUpgrade
import site.siredvin.testiarium.api.TestGroup
import site.siredvin.testiarium.api.thenExecuteFailFast
import site.siredvin.testiarium.cct.CctComputerState
import site.siredvin.tweakium.modules.peripheral.owner.PocketPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.owner.TurtlePeripheralOwner

@TestGroup("ae2-configurable-peripherals")
class AE2WirelessTerminalGameTests {
    @Suppress("DEPRECATION")
    @GameTest(template = "empty")
    fun corruptPocketSubscriptionsAreDiscarded(helper: GameTestHelper) {
        val terminal = AEItems.WIRELESS_TERMINAL.stack()
        WirelessTerminalItem.LINKABLE_HANDLER.link(terminal, GlobalPos.of(helper.level.dimension(), helper.absolutePos(BlockPos(1, 1, 1))))
        val pocketData = PocketUpgrades.instance().get(helper.level.registryAccess(), terminal)
            ?: error("Linked wireless terminal was not accepted as a pocket upgrade")

        var currentUpgrade: UpgradeData<IPocketUpgrade>? = pocketData
        var colour = -1
        var light = -1
        val access = object : IPocketAccess {
            override fun getLevel(): ServerLevel = helper.level
            override fun getPosition(): Vec3 = Vec3.atCenterOf(helper.absolutePos(BlockPos(1, 1, 1)))
            override fun getEntity(): Entity = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL)
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
            override fun getUpgradeData(): DataComponentPatch = currentUpgrade!!.data
            override fun setUpgradeData(data: DataComponentPatch) {
                currentUpgrade = UpgradeData.of(currentUpgrade!!.holder, data)
            }
            override fun invalidatePeripheral() = Unit
        }

        val storage = PocketPeripheralOwner(access).dataStorage
        storage.putCompound("ae2StorageSubscriptions", malformedSubscriptions())
        (pocketData.upgrade() as AE2WirelessTerminalPocketUpgrade).createPeripheral(access)
        val definitions = storage.getCompound("ae2StorageSubscriptions").getList("subscriptions", net.minecraft.nbt.Tag.TAG_COMPOUND.toInt())
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
        helper.level.setBlockAndUpdate(chestPos, AEBlocks.ME_CHEST.block().defaultBlockState())
        (helper.level.getBlockEntity(chestPos) as MEChestBlockEntity).setCell(AEItems.ITEM_CELL_1K.stack())
        val providerPos = energyPos.west()
        helper.level.setBlockAndUpdate(energyPos.above(), AEBlocks.CRAFTING_STORAGE_1K.block().defaultBlockState())
        helper.level.setBlockAndUpdate(providerPos, AEBlocks.PATTERN_PROVIDER.block().defaultBlockState())
        helper.level.setBlockAndUpdate(providerPos.west(), net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState())
        (helper.level.getBlockEntity(providerPos) as PatternProviderBlockEntity).logic.patternInv.setItemDirect(
            0,
            PatternDetailsHelper.encodeProcessingPattern(listOf(GenericStack(AEItemKey.of(Items.DIRT), 1)), listOf(GenericStack(AEItemKey.of(Items.DIAMOND), 1))),
        )

        val terminalItem = AEItems.WIRELESS_CRAFTING_TERMINAL.asItem()
        val terminal = AEItems.WIRELESS_CRAFTING_TERMINAL.stack().apply {
            set(DataComponents.CUSTOM_NAME, Component.literal("Test terminal"))
            set(DataComponents.CUSTOM_DATA, CustomData.of(CompoundTag().apply { putString("upw_test", "preserved") }))
        }
        WirelessTerminalItem.LINKABLE_HANDLER.link(terminal, GlobalPos.of(helper.level.dimension(), accessPointPos))
        terminalItem.injectAEPower(terminal, 400.0, Actionable.MODULATE)
        terminalItem.getUpgrades(terminal).setItemDirect(0, AEItems.ENERGY_CARD.stack())
        val initialCharge = terminalItem.getAECurrentPower(terminal)
        val standardTerminal = AEItems.WIRELESS_TERMINAL.stack()
        check(
            TurtleUpgrades.instance().get(helper.level.registryAccess(), standardTerminal) == null &&
                TurtleUpgrades.instance().get(helper.level.registryAccess(), AEItems.WIRELESS_CRAFTING_TERMINAL.stack()) == null &&
                PocketUpgrades.instance().get(helper.level.registryAccess(), standardTerminal) == null &&
                PocketUpgrades.instance().get(helper.level.registryAccess(), AEItems.WIRELESS_CRAFTING_TERMINAL.stack()) == null,
        ) {
            "Unlinked wireless terminal was accepted as a computer upgrade"
        }
        (standardTerminal.item as WirelessTerminalItem).let {
            WirelessTerminalItem.LINKABLE_HANDLER.link(standardTerminal, GlobalPos.of(helper.level.dimension(), accessPointPos))
            it.injectAEPower(standardTerminal, 100.0, Actionable.MODULATE)
        }
        check(TurtleUpgrades.instance().get(helper.level.registryAccess(), standardTerminal)?.holder?.key()?.location() == AE2WirelessTerminalUpgrade.UPGRADE_ID) {
            "Linked standard wireless terminal was not accepted as a turtle upgrade"
        }
        val standardPocketUpgrade = PocketUpgrades.instance().get(helper.level.registryAccess(), standardTerminal)
        check(standardPocketUpgrade?.holder?.key()?.location() == AE2WirelessTerminalUpgrade.UPGRADE_ID && ItemStack.matches(standardTerminal, standardPocketUpgrade.getUpgradeItem())) {
            "Linked standard wireless terminal was not accepted as a pocket upgrade"
        }
        val upgradeData = TurtleUpgrades.instance().get(helper.level.registryAccess(), terminal)
            ?: error("Linked wireless terminal was not accepted as a turtle upgrade")
        check(upgradeData.holder.key().location() == AE2WirelessTerminalUpgrade.CRAFTING_UPGRADE_ID) {
            "Linked wireless crafting terminal resolved to the wrong turtle upgrade"
        }
        val craftingPocketUpgrade = PocketUpgrades.instance().get(helper.level.registryAccess(), terminal)
        check(craftingPocketUpgrade?.holder?.key()?.location() == AE2WirelessTerminalUpgrade.CRAFTING_UPGRADE_ID && ItemStack.matches(terminal, craftingPocketUpgrade.getUpgradeItem())) {
            "Linked wireless crafting terminal resolved to the wrong pocket upgrade"
        }
        val upgrade = upgradeData.upgrade() as AE2WirelessTerminalUpgrade
        check(ItemStack.matches(terminal, upgrade.getUpgradeItem(upgrade.getUpgradeData(terminal))))
        turtle.access.setUpgrade(TurtleSide.LEFT, upgradeData)
        TurtlePeripheralOwner(turtle.access, TurtleSide.LEFT).dataStorage.putCompound("ae2StorageSubscriptions", malformedSubscriptions())
        turtle.access.setUpgrade(TurtleSide.LEFT, turtle.access.getUpgradeWithData(TurtleSide.LEFT))
        lateinit var grid: IGrid
        var gridSizeWithoutObserver = 0
        var usedChannelsWithoutObserver = 0

        helper.startSequence()
            .thenIdle(10)
            .thenExecuteFailFast {
                check(turtle.access.getPeripheral(TurtleSide.LEFT)!!.additionalTypes.contains("ae2_network_access"))
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
                check(turtle.getItem(0).count == 5 && turtle.getItem(0).`is`(Items.STONE)) { "Invalid slot calls changed turtle stone inventory" }
                check(turtle.getItem(15).count == 5 && turtle.getItem(15).`is`(Items.GOLD_INGOT)) { "Invalid slot calls changed turtle gold inventory" }
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
            .thenWaitUntil {
                grid = (helper.level.getBlockEntity(accessPointPos) as WirelessAccessPointBlockEntity).grid!!
                if (!grid.craftingService.isCraftable(AEItemKey.of(Items.DIAMOND))) throw GameTestAssertException("Processing pattern is not yet available after network rejoin")
            }
            .thenExecuteFailFast {
                state().check("reactivated")
                check(grid.storageService.inventory.insert(AEItemKey.of(Items.DIRT), 4, Actionable.MODULATE, IActionSource.empty()) == 4L) { "Failed to seed crafting ingredients after network rejoin" }
            }
            .thenWaitUntil { await("crafting-canceled") }
            .thenWaitUntil { if (grid.craftingService.cpus.any { it.isBusy }) throw GameTestAssertException("Crafting CPU is still busy after cancellation") }
            .thenExecuteFailFast {
                state().check("crafting-canceled")
                check(grid.storageService.inventory.insert(AEItemKey.of(Items.IRON_NUGGET), 1, Actionable.MODULATE, IActionSource.empty()) == 1L)
            }
            .thenWaitUntil { await("crafting-running") }
            .thenWaitUntil { if (!grid.craftingService.isRequesting(AEItemKey.of(Items.DIAMOND))) throw GameTestAssertException("Crafting CPU has not requested output") }
            .thenExecuteFailFast {
                state().check("crafting-running")
                // Simulate the external processing machine returning its output through AE2 storage.
                check(grid.storageService.inventory.insert(AEItemKey.of(Items.DIAMOND), 1, Actionable.MODULATE, IActionSource.empty()) == 1L)
            }
            .thenWaitUntil { await("crafting-complete") }
            .thenExecuteFailFast {
                state().check("crafting-complete")
                val stored = turtle.access.getUpgradeWithData(TurtleSide.LEFT)!!.upgradeItem
                WirelessTerminalItem.LINKABLE_HANDLER.link(stored, GlobalPos.of(helper.level.dimension(), UNLOADED_POS))
                turtle.access.setUpgradeData(TurtleSide.LEFT, stored.componentsPatch)
                check(helper.level.chunkSource.getChunkNow(UNLOADED_POS.x shr 4, UNLOADED_POS.z shr 4) == null)
            }
            .thenWaitUntil { await(CctComputerState.DONE) }
            .thenIdle(2)
            .thenExecuteFailFast {
                state().check(CctComputerState.DONE)
                check(helper.level.chunkSource.getChunkNow(UNLOADED_POS.x shr 4, UNLOADED_POS.z shr 4) == null) { "Wireless resolution loaded the linked chunk" }
                val stored = turtle.access.getUpgradeWithData(TurtleSide.LEFT)!!.upgradeItem
                check(stored.hoverName.string == "Test terminal")
                check(stored.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("upw_test") == "preserved")
                check(terminalItem.getUpgrades(stored).getInstalledUpgrades(AEItems.ENERGY_CARD) == 1)
                check(terminalItem.getAECurrentPower(stored) == initialCharge) { "Peripheral use changed terminal charge" }
                val currentGrid = (helper.level.getBlockEntity(accessPointPos) as WirelessAccessPointBlockEntity).grid!!
                check(currentGrid.size() == gridSizeWithoutObserver) { "Wireless storage observer was not cleaned up" }
                val definitions = TurtlePeripheralOwner(turtle.access, TurtleSide.LEFT).dataStorage.getCompound("ae2StorageSubscriptions").getList("subscriptions", net.minecraft.nbt.Tag.TAG_COMPOUND.toInt())
                check(definitions.size == 1 && definitions.getCompound(0).getString("name") == "stone") { "Corrupt turtle subscriptions were not replaced with valid persisted data" }
            }
            .thenSucceed()
    }

    private fun state() = CctComputerState.get(FIXTURE) ?: throw GameTestAssertException("Computer '$FIXTURE' has not started")

    private fun findTurtle(helper: GameTestHelper): TurtleBlockEntity {
        for (x in 0 until 7) {
            for (y in 0 until 4) {
                for (z in 0 until 7) {
                    val entity = helper.level.getBlockEntity(helper.absolutePos(BlockPos(x, y, z)))
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
