package site.siredvin.peripheralworks.testmod

import appeng.api.crafting.PatternDetailsHelper
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.GenericStack
import appeng.core.definitions.AEItems
import dan200.computercraft.shared.computer.blocks.ComputerBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestAssertException
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.entity.ChestBlockEntity
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import site.siredvin.peripheralworks.integrations.ae2.AE2PatternPedestalBlockEntity
import site.siredvin.peripheralworks.integrations.ae2.AE2PatternPedestalPeripheral
import site.siredvin.peripheralworks.integrations.ae2.Registration
import site.siredvin.testiarium.api.TestGroup
import site.siredvin.testiarium.api.thenExecuteFailFast
import site.siredvin.testiarium.cct.CctComputerState

@TestGroup("ae2-configurable-peripherals")
class AE2PatternPedestalGameTests {
    @GameTest(template = "peripheralworksgametests.ae2_configurable_objects", timeoutTicks = 2400)
    fun luaApi(helper: GameTestHelper) {
        val computer = (0..4).flatMap { x -> (0..3).flatMap { y -> (0..4).map { z -> helper.getBlockEntity(BlockPos(x, y, z)) } } }.filterIsInstance<ComputerBlockEntity>().single()
        val label = "peripheralworksgametests.ae2_pattern_pedestal"
        computer.setLabel(label)
        helper.level.setBlockAndUpdate(computer.blockPos.relative(Direction.NORTH), Registration.PATTERN_PEDESTAL.get().defaultBlockState())
        val chestPos = computer.blockPos.relative(Direction.WEST)
        helper.level.setBlockAndUpdate(chestPos, net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState())
        (helper.level.getBlockEntity(chestPos) as ChestBlockEntity).apply {
            setItem(0, AEItems.BLANK_PATTERN.stack(64))
            setItem(1, ItemStack(Items.DIRT, 64))
        }
        helper.startSequence().thenIdle(5)
            .thenExecute { computer.createServerComputer().turnOn() }
            .thenWaitUntil {
                val state = CctComputerState.get(label) ?: throw GameTestAssertException("Computer has not started")
                if (!state.isDone(CctComputerState.DONE)) throw GameTestAssertException("Lua has not completed")
                state.check(CctComputerState.DONE)
            }
            .thenExecuteFailFast {
                val chest = helper.level.getBlockEntity(chestPos) as ChestBlockEntity
                check(chest.getItem(0).count == 63 && chest.getItem(1).count == 64 && chest.getItem(2).count == 1)
            }
            .thenSucceed()
    }

    @GameTest(template = "empty")
    fun invalidPatternsPersistenceAndInteraction(helper: GameTestHelper) {
        val pos = BlockPos(1, 1, 1)
        helper.setBlock(pos, Registration.PATTERN_PEDESTAL.get())
        val entity = helper.getBlockEntity(pos) as AE2PatternPedestalBlockEntity
        val peripheral = AE2PatternPedestalPeripheral(entity)
        val player = helper.makeMockPlayer()
        player.setItemInHand(InteractionHand.MAIN_HAND, AEItems.BLANK_PATTERN.stack(64))
        val absolute = helper.absolutePos(pos)

        @Suppress("DEPRECATION")
        val used = Registration.PATTERN_PEDESTAL.get().use(entity.blockState, helper.level, absolute, player, InteractionHand.MAIN_HAND, BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false))
        check(used == InteractionResult.CONSUME && player.mainHandItem.count == 63 && entity.storedStack.count == 1)
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY)
        @Suppress("DEPRECATION")
        Registration.PATTERN_PEDESTAL.get().attack(entity.blockState, helper.level, absolute, player)
        check(entity.storedStack.isEmpty && player.mainHandItem.`is`(AEItems.BLANK_PATTERN.asItem()) && player.mainHandItem.count == 1)
        entity.storage.store(player.mainHandItem.copy(), false)
        val processing = PatternDetailsHelper.encodeProcessingPattern(arrayOf(GenericStack(AEItemKey.of(Items.STONE), 1)), arrayOf(GenericStack(AEItemKey.of(Items.DIAMOND), 1)))
        check(entity.replacePattern(entity.storedStack.copy(), processing))
        val saved = entity.saveWithoutMetadata()
        val loaded = AE2PatternPedestalBlockEntity(absolute, entity.blockState)
        loaded.load(saved)
        check(ItemStack.matches(entity.storedStack, loaded.storedStack))
        val synchronized = AE2PatternPedestalBlockEntity(absolute, entity.blockState)
        synchronized.load(entity.updateTag)
        check(ItemStack.matches(entity.storedStack, synchronized.storedStack))
        val beforeRead = entity.storedStack.copy()
        check(peripheral.getPattern()["state"] == "encoded" && ItemStack.matches(entity.storedStack, beforeRead))
        for (item in listOf(AEItems.CRAFTING_PATTERN.asItem(), AEItems.PROCESSING_PATTERN.asItem(), AEItems.STONECUTTING_PATTERN.asItem(), AEItems.SMITHING_TABLE_PATTERN.asItem())) {
            check(entity.replacePattern(entity.storedStack.copy(), ItemStack(item)))
            check(peripheral.getPattern()["state"] == "invalid")
            check(peripheral.encodeProcessingPattern(emptyMap<Any, Any>()).result!![0] == null)
            check(peripheral.encodeCraftingPattern(emptyMap<Any, Any>()).result!![0] == null)
            check(peripheral.encodeStonecuttingPattern(emptyMap<Any, Any>()).result!![0] == null)
            check(peripheral.encodeSmithingTablePattern(emptyMap<Any, Any>()).result!![0] == null)
            check(peripheral.clearPattern().result!![0] == true)
            check(entity.storedStack.`is`(AEItems.BLANK_PATTERN.asItem()) && entity.storedStack.count == 1)
        }
        val overCount = processing.copyWithCount(2)
        // Load deliberately malformed data through the real save format without truncating it.
        val unconstrained = site.siredvin.peripheralworks.xplat.ModPlatform.baseInnerPlatform.createSlottedItemStorage(1, 2, {})
        unconstrained.second.store(overCount.copy(), false)
        entity.loadInternalData(CompoundTag().apply { put("storedItemStackV2", unconstrained.first.save()) }, null)
        check(entity.storedStack.count == 2)
        check(peripheral.clearPattern().result!![0] == null && entity.storedStack.count == 2)
        check(peripheral.encodeProcessingPattern(emptyMap<Any, Any>()).result!![0] == null)
        helper.level.destroyBlock(absolute, true)
        val drops = helper.level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity::class.java, net.minecraft.world.phys.AABB(absolute).inflate(1.0))
        check(drops.filter { it.item.`is`(AEItems.PROCESSING_PATTERN.asItem()) }.sumOf { it.item.count } == 2)
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun nativeRecipesAndLimits(helper: GameTestHelper) {
        val pos = BlockPos(1, 1, 1)
        helper.setBlock(pos, Registration.PATTERN_PEDESTAL.get())
        val entity = helper.getBlockEntity(pos) as AE2PatternPedestalBlockEntity
        val peripheral = AE2PatternPedestalPeripheral(entity)
        entity.storage.store(AEItems.BLANK_PATTERN.stack(), false)
        fun item(stack: ItemStack) = site.siredvin.peripheralworks.integrations.ae2.AE2Helper.keyToMap(AEItemKey.of(stack)!!) + mapOf("count" to 1, "snbt" to (stack.tag ?: CompoundTag()).toString())
        fun ingredient(value: net.minecraft.world.item.Item) = item(ItemStack(value))
        fun unchanged(action: () -> Unit) {
            val before = entity.storedStack.copy()
            action()
            check(ItemStack.matches(before, entity.storedStack)) { "Rejected call changed blank NBT" }
        }
        entity.storedStack.orCreateTag.putString("preserve", "blank tag")
        val output = mapOf(1 to ingredient(Items.DIAMOND))
        for (count in listOf(Double.NaN, Double.POSITIVE_INFINITY, 0.0, -1.0, 0.5, 9_007_199_254_740_992.0)) {
            unchanged {
                try {
                    peripheral.encodeProcessingPattern(mapOf("inputs" to mapOf(1 to (ingredient(Items.STONE) + ("count" to count))), "outputs" to output))
                    error("Unsafe count accepted")
                } catch (_: dan200.computercraft.api.lua.LuaException) { }
            }
        }
        for ((inputs, outputs) in listOf(
            (1..82).associateWith { ingredient(Items.STONE) } to output,
            mapOf(1 to ingredient(Items.STONE)) to (1..28).associateWith { ingredient(Items.DIAMOND) },
            mapOf(2 to ingredient(Items.STONE)) to output,
        )) {
            unchanged {
                try {
                    peripheral.encodeProcessingPattern(mapOf("inputs" to inputs, "outputs" to outputs))
                    error("Invalid native array accepted")
                } catch (_: dan200.computercraft.api.lua.LuaException) { }
            }
        }
        val largeFluidCount = 9_007_199_254_740_991L
        val fluidOutput = mapOf(1 to mapOf("type" to "fluid", "name" to "minecraft:water", "count" to largeFluidCount))
        check(peripheral.encodeProcessingPattern(mapOf("inputs" to mapOf(1 to ingredient(Items.STONE)), "outputs" to fluidOutput)).result!![0] == true)
        val summary = (peripheral.getPattern()["outputs"] as List<*>).single() as Map<*, *>
        check((summary["amount"] as Number).toLong() == largeFluidCount) { "Fluid summary lost millibucket precision" }
        check(peripheral.clearPattern().result!![0] == true)
        val cakeItems = listOf(Items.MILK_BUCKET, Items.MILK_BUCKET, Items.MILK_BUCKET, Items.SUGAR, Items.EGG, Items.SUGAR, Items.WHEAT, Items.WHEAT, Items.WHEAT)
        check(peripheral.encodeCraftingPattern(mapOf("recipeId" to "minecraft:cake", "grid" to cakeItems.mapIndexed { index, value -> index + 1 to ingredient(value) }.toMap())).result!![0] == true)
        val cake = PatternDetailsHelper.decodePattern(entity.storedStack.copy(), helper.level, false) as appeng.crafting.pattern.AECraftingPattern
        val grid = net.minecraft.world.inventory.TransientCraftingContainer(appeng.menu.AutoCraftingMenu(), 3, 3)
        cakeItems.forEachIndexed { index, value -> grid.setItem(index, ItemStack(value)) }
        check(cake.getRemainingItems(grid).count { it.`is`(Items.BUCKET) } == 3)
        val missing = entity.storedStack.copy().apply { orCreateTag.putString("recipe", "minecraft:missing_recipe") }
        check(entity.replacePattern(entity.storedStack.copy(), missing))
        check(peripheral.getPattern()["state"] == "invalid" && ItemStack.matches(missing, entity.storedStack))
        check(peripheral.clearPattern().result!![0] == true)
        val input = ItemStack(Items.STONE)
        val cutting = helper.level.recipeManager.getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.STONECUTTING).filter { it.matches(net.minecraft.world.SimpleContainer(input), helper.level) }.take(2)
        check(cutting.size == 2)
        for (recipe in cutting) {
            check(peripheral.encodeStonecuttingPattern(mapOf("recipeId" to recipe.id.toString(), "input" to item(input), "substitute" to true)).result!![0] == true)
            val native = PatternDetailsHelper.decodePattern(entity.storedStack.copy(), helper.level, false) as appeng.crafting.pattern.AEStonecuttingPattern
            check(native.recipeId == recipe.id && native.canSubstitute)
            check((native.outputs.single().what as AEItemKey) == AEItemKey.of(recipe.assemble(net.minecraft.world.SimpleContainer(input), helper.level.registryAccess())))
            check(peripheral.clearPattern().result!![0] == true)
        }
        val template = ItemStack(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE)
        val base = ItemStack(Items.IRON_CHESTPLATE).apply { orCreateTag.putInt("Damage", 7) }
        val addition = ItemStack(Items.DIAMOND)
        val smithingInputs = net.minecraft.world.SimpleContainer(template, base, addition)
        val trim = helper.level.recipeManager.getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.SMITHING).first { it.matches(smithingInputs, helper.level) }
        val smithing = mapOf("recipeId" to trim.id.toString(), "template" to item(template), "base" to item(base), "addition" to item(addition), "substitute" to true)
        check(peripheral.encodeSmithingTablePattern(smithing).result!![0] == true)
        val encodedTrim = PatternDetailsHelper.decodePattern(entity.storedStack.copy(), helper.level, false) as appeng.crafting.pattern.AESmithingTablePattern
        check(encodedTrim.canSubstitute && encodedTrim.recipeId == trim.id)
        check(encodedTrim.outputs.single().what == AEItemKey.of(trim.assemble(smithingInputs, helper.level.registryAccess())))
        check(peripheral.clearPattern().result!![0] == true)
        for (field in listOf("template", "base", "addition")) {
            unchanged {
                check(peripheral.encodeSmithingTablePattern(smithing + (field to ingredient(Items.DIRT))).result!![0] == null)
            }
        }
        helper.succeed()
    }
}
