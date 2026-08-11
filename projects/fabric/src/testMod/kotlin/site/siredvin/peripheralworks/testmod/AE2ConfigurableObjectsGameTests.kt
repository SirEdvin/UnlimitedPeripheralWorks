package site.siredvin.peripheralworks.testmod

import appeng.api.crafting.PatternDetailsHelper
import appeng.api.networking.crafting.ICraftingService
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.GenericStack
import appeng.blockentity.networking.CableBusBlockEntity
import appeng.core.definitions.AEBlocks
import appeng.core.definitions.AEItems
import appeng.core.definitions.AEParts
import appeng.helpers.MultiCraftingTracker
import appeng.parts.automation.ExportBusPart
import dan200.computercraft.shared.computer.blocks.ComputerBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestAssertException
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.entity.ChestBlockEntity
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.testiarium.api.TestGroup
import site.siredvin.testiarium.api.thenExecuteFailFast
import site.siredvin.testiarium.cct.CctComputerState
import java.lang.reflect.Proxy
import java.util.concurrent.CompletableFuture

@TestGroup("ae2-configurable-peripherals")
class AE2ConfigurableObjectsGameTests {
    @GameTest(template = FIXTURE, batch = FIXTURE, timeoutTicks = 2400)
    fun configurableObjects(helper: GameTestHelper) {
        val computer = findComputer(helper)
        val interfacePos = computer.blockPos.relative(Direction.NORTH)
        val cablePos = computer.blockPos.relative(Direction.SOUTH)
        val chestPos = computer.blockPos.relative(Direction.WEST)
        val patternProviderPos = computer.blockPos.relative(Direction.EAST)
        helper.level.setBlockAndUpdate(interfacePos, AEBlocks.INTERFACE.block().defaultBlockState())
        helper.level.setBlockAndUpdate(patternProviderPos, AEBlocks.PATTERN_PROVIDER.block().defaultBlockState())
        helper.level.setBlockAndUpdate(cablePos, AEBlocks.CABLE_BUS.block().defaultBlockState())
        helper.level.setBlockAndUpdate(chestPos, net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState())
        val cable = helper.level.getBlockEntity(cablePos) as CableBusBlockEntity
        val exportBus = cable.addPart(AEParts.EXPORT_BUS.asItem(), Direction.SOUTH, null)!!
        assertCraftingSlotTen(exportBus, helper)
        cable.addPart(AEParts.STORAGE_BUS.asItem(), Direction.EAST, null)
        cable.addPart(AEParts.FORMATION_PLANE.asItem(), Direction.WEST, null)
        cable.addPart(AEParts.LEVEL_EMITTER.asItem(), Direction.UP, null)
        cable.addPart(AEParts.ENERGY_LEVEL_EMITTER.asItem(), Direction.DOWN, null)
        check(ComputerCraftProxy.collectPlugins(helper.level, cablePos, Direction.NORTH).containsKey("ae2_cable_objects")) {
            "Cable provider was not registered"
        }
        check(ComputerCraftProxy.collectPlugins(helper.level, interfacePos, Direction.SOUTH).containsKey("ae2_interface_object")) {
            "Interface provider was not registered"
        }
        check(ComputerCraftProxy.collectPlugins(helper.level, patternProviderPos, Direction.WEST).containsKey("ae2_pattern_provider_object")) {
            "Pattern Provider provider was not registered"
        }

        (helper.level.getBlockEntity(chestPos) as ChestBlockEntity).apply {
            setItem(0, AEItems.CAPACITY_CARD.stack())
            setItem(2, AEItems.CRAFTING_CARD.stack())
            setItem(3, AEItems.FUZZY_CARD.stack())
            setItem(4, ItemStack(Items.STONE))
            setItem(
                5,
                PatternDetailsHelper.encodeProcessingPattern(
                    arrayOf(GenericStack(AEItemKey.of(Items.COBBLESTONE), 1)),
                    arrayOf(GenericStack(AEItemKey.of(Items.STONE), 1)),
                ),
            )
        }
        helper.startSequence()
            .thenIdle(5)
            .thenExecute { computer.createServerComputer().turnOn() }
            .thenWaitUntil { await("same-kind") }
            .thenExecuteFailFast {
                state().check("same-kind")
                cable.removePartFromSide(Direction.SOUTH)
                check(cable.addPart(AEParts.EXPORT_BUS.asItem(), Direction.SOUTH, null) != null)
            }
            .thenWaitUntil { await("different-kind") }
            .thenExecuteFailFast {
                state().check("different-kind")
                cable.removePartFromSide(Direction.SOUTH)
                check(cable.addPart(AEParts.IMPORT_BUS.asItem(), Direction.SOUTH, null) != null)
            }
            .thenWaitUntil { await("removed") }
            .thenExecuteFailFast {
                state().check("removed")
                cable.removePartFromSide(Direction.SOUTH)
            }
            .thenWaitUntil { await(CctComputerState.DONE) }
            .thenExecuteFailFast { state().check(CctComputerState.DONE) }
            .thenSucceed()
    }

    private fun state() = CctComputerState.get(FIXTURE) ?: throw GameTestAssertException("Computer '$FIXTURE' has not started")

    private fun await(marker: String) {
        val state = CctComputerState.get(FIXTURE) ?: throw GameTestAssertException("Computer '$FIXTURE' has not started")
        if (state.isDone(CctComputerState.DONE)) state.check(CctComputerState.DONE)
        if (!state.isDone(marker)) throw GameTestAssertException("Computer '$FIXTURE' has not reached $marker")
    }

    private fun findComputer(helper: GameTestHelper): ComputerBlockEntity {
        for (x in 0 until 5) {
            for (y in 0 until 4) {
                for (z in 0 until 5) {
                    val entity = helper.getBlockEntity(BlockPos(x, y, z))
                    if (entity is ComputerBlockEntity) return entity
                }
            }
        }
        throw GameTestAssertException("Fixture computer is missing")
    }

    private fun assertCraftingSlotTen(exportBus: ExportBusPart, helper: GameTestHelper) {
        val field = ExportBusPart::class.java.getDeclaredField("craftingTracker").apply { isAccessible = true }
        val tracker = field.get(exportBus) as MultiCraftingTracker
        val craftingService = Proxy.newProxyInstance(
            ICraftingService::class.java.classLoader,
            arrayOf(ICraftingService::class.java),
        ) { _, method, _ ->
            if (method.name == "beginCraftingCalculation") CompletableFuture.completedFuture<Any?>(null) else error("Unexpected ${method.name}")
        } as ICraftingService
        tracker.handleCrafting(9, AEItemKey.of(Items.STONE), 1, helper.level, craftingService, IActionSource.empty())
    }

    companion object {
        private const val FIXTURE = "peripheralworksgametests.ae2_configurable_objects"
    }
}
