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
    fun interfaceObject(helper: GameTestHelper) = configurableObject(helper, Device.INTERFACE)

    @GameTest(template = FIXTURE, batch = FIXTURE, timeoutTicks = 2400)
    fun importBus(helper: GameTestHelper) = configurableObject(helper, Device.IMPORT_BUS)

    @GameTest(template = FIXTURE, batch = FIXTURE, timeoutTicks = 2400)
    fun exportBus(helper: GameTestHelper) = configurableObject(helper, Device.EXPORT_BUS)

    @GameTest(template = FIXTURE, batch = FIXTURE, timeoutTicks = 2400)
    fun storageBus(helper: GameTestHelper) = configurableObject(helper, Device.STORAGE_BUS)

    @GameTest(template = FIXTURE, batch = FIXTURE, timeoutTicks = 2400)
    fun formationPlane(helper: GameTestHelper) = configurableObject(helper, Device.FORMATION_PLANE)

    @GameTest(template = FIXTURE, batch = FIXTURE, timeoutTicks = 2400)
    fun storageLevelEmitter(helper: GameTestHelper) = configurableObject(helper, Device.STORAGE_LEVEL_EMITTER)

    @GameTest(template = FIXTURE, batch = FIXTURE, timeoutTicks = 2400)
    fun energyLevelEmitter(helper: GameTestHelper) = configurableObject(helper, Device.ENERGY_LEVEL_EMITTER)

    @GameTest(template = FIXTURE, batch = FIXTURE, timeoutTicks = 2400)
    fun patternProvider(helper: GameTestHelper) = configurableObject(helper, Device.PATTERN_PROVIDER)

    private fun configurableObject(helper: GameTestHelper, device: Device) {
        val computer = findComputer(helper)
        val label = "peripheralworksgametests.ae2_${device.name.lowercase()}"
        computer.setLabel(label)
        val targetPos = computer.blockPos.relative(Direction.NORTH)
        val chestPos = computer.blockPos.relative(Direction.WEST)
        val directBlock = when (device) {
            Device.INTERFACE -> AEBlocks.INTERFACE.block()
            Device.PATTERN_PROVIDER -> AEBlocks.PATTERN_PROVIDER.block()
            else -> null
        }
        helper.level.setBlockAndUpdate(targetPos, (directBlock ?: AEBlocks.CABLE_BUS.block()).defaultBlockState())
        helper.level.setBlockAndUpdate(chestPos, net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState())
        if (directBlock == null) {
            val cable = helper.level.getBlockEntity(targetPos) as CableBusBlockEntity
            val part = when (device) {
                Device.IMPORT_BUS -> AEParts.IMPORT_BUS
                Device.EXPORT_BUS -> AEParts.EXPORT_BUS
                Device.STORAGE_BUS -> AEParts.STORAGE_BUS
                Device.FORMATION_PLANE -> AEParts.FORMATION_PLANE
                Device.STORAGE_LEVEL_EMITTER -> AEParts.LEVEL_EMITTER
                Device.ENERGY_LEVEL_EMITTER -> AEParts.ENERGY_LEVEL_EMITTER
                else -> error("Unexpected direct device $device")
            }
            val added = cable.addPart(part.asItem(), Direction.SOUTH, null)!!
            if (added is ExportBusPart) assertCraftingSlotTen(added, helper)
        }
        val provider = if (directBlock == null) {
            "ae2_cable_objects"
        } else if (device == Device.INTERFACE) {
            "ae2_interface_object"
        } else {
            "ae2_pattern_provider_object"
        }
        check(ComputerCraftProxy.collectPlugins(helper.level, targetPos, Direction.SOUTH).containsKey(provider)) { "$device provider was not registered" }

        (helper.level.getBlockEntity(chestPos) as ChestBlockEntity).apply {
            setItem(0, AEItems.CAPACITY_CARD.stack())
            setItem(2, AEItems.CRAFTING_CARD.stack())
            setItem(3, AEItems.FUZZY_CARD.stack())
            setItem(4, ItemStack(Items.STONE))
            setItem(5, PatternDetailsHelper.encodeProcessingPattern(arrayOf(GenericStack(AEItemKey.of(Items.COBBLESTONE), 1)), arrayOf(GenericStack(AEItemKey.of(Items.STONE), 1))))
        }
        helper.startSequence()
            .thenIdle(5)
            .thenExecute { computer.createServerComputer().turnOn() }
            .thenWaitUntil { await(label, CctComputerState.DONE) }
            .thenExecuteFailFast { state(label).check(CctComputerState.DONE) }
            .thenSucceed()
    }

    private fun state(label: String) = CctComputerState.get(label) ?: throw GameTestAssertException("Computer '$label' has not started")

    private fun await(label: String, marker: String) {
        val state = state(label)
        if (state.isDone(CctComputerState.DONE)) state.check(CctComputerState.DONE)
        if (!state.isDone(marker)) throw GameTestAssertException("Computer '$label' has not reached $marker")
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

    private enum class Device { INTERFACE, IMPORT_BUS, EXPORT_BUS, STORAGE_BUS, FORMATION_PLANE, STORAGE_LEVEL_EMITTER, ENERGY_LEVEL_EMITTER, PATTERN_PROVIDER }

    companion object {
        private const val FIXTURE = "peripheralworksgametests.ae2_configurable_objects"
    }
}
