package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.crafting.PatternDetailsHelper
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.GenericStack
import appeng.core.definitions.AEItems
import appeng.crafting.pattern.AECraftingPattern
import appeng.crafting.pattern.AEProcessingPattern
import appeng.crafting.pattern.AESmithingTablePattern
import appeng.crafting.pattern.AEStonecuttingPattern
import appeng.menu.AutoCraftingMenu
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.SimpleContainer
import net.minecraft.world.inventory.TransientCraftingContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.SmithingRecipe
import net.minecraft.world.item.crafting.StonecutterRecipe
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralworks.common.block.AbstractItemPedestal
import site.siredvin.peripheralworks.common.block.BasePedestal
import site.siredvin.peripheralworks.common.blockentity.AbstractItemPedestalBlockEntity
import site.siredvin.peripheralworks.computercraft.plugins.PedestalInventoryPlugin
import site.siredvin.tweakium.modules.peripheral.OwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation
import java.util.function.Predicate

class AE2PatternPedestal : AbstractItemPedestal<AE2PatternPedestalBlockEntity>() {
    override fun newBlockEntity(blockPos: BlockPos, blockState: BlockState) = AE2PatternPedestalBlockEntity(blockPos, blockState)
}

class AE2PatternPedestalBlockEntity(pos: BlockPos, state: BlockState) :
    AbstractItemPedestalBlockEntity<AE2PatternPedestalPeripheral>(
        Registration.PATTERN_PEDESTAL_BLOCK_ENTITY.get(),
        pos,
        state,
        capacity = 1,
        accepts = { it.`is`(AEItems.BLANK_PATTERN.asItem()) || patternType(it) != null },
    ) {
    override val itemFilter: Predicate<ItemStack> = Predicate { it.`is`(AEItems.BLANK_PATTERN.asItem()) || patternType(it) != null }
    override fun createPeripheral(side: Direction) = AE2PatternPedestalPeripheral(this)
    fun replacePattern(expected: ItemStack, replacement: ItemStack): Boolean = replaceStoredStack(expected, replacement)
}

private fun patternType(stack: ItemStack): String? = when (stack.item) {
    AEItems.CRAFTING_PATTERN.asItem() -> "crafting"
    AEItems.PROCESSING_PATTERN.asItem() -> "processing"
    AEItems.STONECUTTING_PATTERN.asItem() -> "stonecutting"
    AEItems.SMITHING_TABLE_PATTERN.asItem() -> "smithing"
    else -> null
}

class AE2PatternPedestalPeripheral(private val entity: AE2PatternPedestalBlockEntity) :
    OwnedPeripheral<BlockEntityPeripheralOwner<AE2PatternPedestalBlockEntity>>(
        "peripheralworks:ae2_pattern_pedestal",
        BlockEntityPeripheralOwner(entity, facingProperty = BasePedestal.FACING),
    ) {
    init {
        addPlugin(PedestalInventoryPlugin(entity))
    }

    override val isEnabled: Boolean get() = true

    @LuaFunction(mainThread = true)
    fun getPattern(): Map<String, Any> {
        val stack = entity.storedStack.copy()
        if (stack.isEmpty) return mapOf("state" to "empty")
        val result = mutableMapOf<String, Any>("item" to LuaRepresentation.forItemStack(stack))
        if (stack.count == 1 && stack.`is`(AEItems.BLANK_PATTERN.asItem())) return result + ("state" to "blank")
        val type = patternType(stack)
        if (type != null) result["type"] = type
        return try {
            check(stack.count == 1 && type != null) { "Expected exactly one native AE2 pattern" }
            // false disables AE2's recovery, which may rewrite the encoded item.
            val pattern = PatternDetailsHelper.decodePattern(stack, entity.level, false) ?: error("Pattern cannot be decoded; its recipe may be missing")
            val definition = when (pattern) {
                is AEProcessingPattern -> mapOf(
                    "inputs" to pattern.sparseInputs.filterNotNull().map(AE2PatternDefinition::describe),
                    "outputs" to pattern.sparseOutputs.filterNotNull().map(AE2PatternDefinition::describe),
                )
                is AECraftingPattern -> mapOf(
                    // AE2's package-private CraftingPatternEncoding stores the recipe ID here.
                    "recipeId" to stack.tag!!.getString("recipe"),
                    "grid" to pattern.sparseInputs.mapIndexedNotNull { index, input -> input?.let { index + 1 to AE2PatternDefinition.describe(it) } }.toMap(),
                    "substitute" to pattern.canSubstitute,
                    "substituteFluids" to pattern.canSubstituteFluids,
                )
                is AEStonecuttingPattern -> mapOf(
                    "recipeId" to pattern.recipeId.toString(),
                    "input" to AE2PatternDefinition.describe(GenericStack(pattern.input, 1)),
                    "substitute" to pattern.canSubstitute,
                )
                is AESmithingTablePattern -> mapOf(
                    "recipeId" to pattern.recipeId.toString(),
                    "template" to AE2PatternDefinition.describe(GenericStack(pattern.template, 1)),
                    "base" to AE2PatternDefinition.describe(GenericStack(pattern.base, 1)),
                    "addition" to AE2PatternDefinition.describe(GenericStack(pattern.addition, 1)),
                    "substitute" to pattern.canSubstitute,
                )
                else -> error("Unsupported native pattern")
            }
            pattern.inputs.forEach { input -> input.possibleInputs.forEach { AE2PatternDefinition.describe(GenericStack(it.what, Math.multiplyExact(it.amount, input.multiplier))) } }
            pattern.outputs.forEach(AE2PatternDefinition::describe)
            result + AE2Helper.patternToMap(pattern) + mapOf("state" to "encoded", "definition" to definition)
        } catch (exception: Exception) {
            result + mapOf("state" to "invalid", "error" to (exception.message ?: "Cannot inspect pattern"))
        }
    }

    @LuaFunction(mainThread = true)
    fun clearPattern(): MethodResult {
        val stack = entity.storedStack.copy()
        if (stack.count != 1) return MethodResult.of(null, "Expected exactly one pattern")
        if (stack.`is`(AEItems.BLANK_PATTERN.asItem())) return MethodResult.of(false)
        if (patternType(stack) == null) return MethodResult.of(null, "Expected a native AE2 pattern")
        return if (entity.replacePattern(stack, AEItems.BLANK_PATTERN.stack())) MethodResult.of(true) else MethodResult.of(null, "Pattern changed")
    }

    private fun encode(definition: Map<*, *>, type: String): MethodResult {
        val original = entity.storedStack.copy()
        if (original.count != 1 || !original.`is`(AEItems.BLANK_PATTERN.asItem())) return MethodResult.of(null, "A blank pattern is required; clearPattern() before encoding")
        return try {
            val candidate = buildPattern(definition, type)
            if (PatternDetailsHelper.decodePattern(candidate, entity.level, false) == null) return MethodResult.of(null, "AE2 rejected the pattern")
            if (!entity.replacePattern(original, candidate)) return MethodResult.of(null, "Pattern changed")
            MethodResult.of(true)
        } catch (exception: LuaException) {
            throw exception
        } catch (exception: IllegalArgumentException) {
            MethodResult.of(null, exception.message ?: "Invalid pattern")
        } catch (exception: IllegalStateException) {
            MethodResult.of(null, exception.message ?: "Invalid pattern")
        }
    }

    private fun buildPattern(definition: Map<*, *>, type: String): ItemStack {
        val level = entity.level ?: error("Pedestal is unavailable")
        if (type == "processing") {
            AE2PatternDefinition.fields(definition, "inputs", "outputs")
            val inputs = AE2PatternDefinition.slots(definition["inputs"], "inputs", AEProcessingPattern.MAX_INPUT_SLOTS).values.map(AE2PatternDefinition::resource).toTypedArray()
            val outputs = AE2PatternDefinition.slots(definition["outputs"], "outputs", AEProcessingPattern.MAX_OUTPUT_SLOTS).values.map(AE2PatternDefinition::resource).toTypedArray()
            // AE2 condenses duplicates using long arithmetic; reject overflow before native decoding.
            for (stacks in listOf(inputs, outputs)) {
                val amounts = mutableMapOf<appeng.api.stacks.AEKey, Long>()
                for (stack in stacks) {
                    val amount = try {
                        Math.addExact(amounts[stack.what] ?: 0L, stack.amount)
                    } catch (_: ArithmeticException) {
                        throw LuaException("Combined resource amount is too large")
                    }
                    AE2PatternDefinition.describe(GenericStack(stack.what, amount))
                    amounts[stack.what] = amount
                }
            }
            return PatternDetailsHelper.encodeProcessingPattern(inputs, outputs)
        }
        when (type) {
            "crafting" -> AE2PatternDefinition.fields(definition, "recipeId", "grid", "substitute", "substituteFluids")
            "stonecutting" -> AE2PatternDefinition.fields(definition, "recipeId", "input", "substitute")
            "smithing" -> AE2PatternDefinition.fields(definition, "recipeId", "template", "base", "addition", "substitute")
        }
        val recipeName = definition["recipeId"] as? String ?: throw LuaException("recipeId must be a registry ID")
        val id = ResourceLocation.tryParse(recipeName) ?: throw LuaException("Invalid recipe ID")
        val recipe = level.recipeManager.byKey(id).orElse(null) ?: error("Recipe not found: $id")
        val substitute = AE2PatternDefinition.flag(definition, "substitute")
        return when (type) {
            "crafting" -> {
                check(recipe is CraftingRecipe) { "Expected a crafting recipe" }
                val inputs = Array(9) { ItemStack.EMPTY }
                AE2PatternDefinition.slots(definition["grid"], "grid", 9, true).forEach { (slot, item) -> inputs[slot - 1] = AE2PatternDefinition.item(item) }
                val grid = TransientCraftingContainer(AutoCraftingMenu(), 3, 3)
                inputs.forEachIndexed { index, item -> grid.setItem(index, item) }
                check(recipe.matches(grid, level)) { "Inputs do not match recipe" }
                val output = recipe.assemble(grid, level.registryAccess())
                check(!output.isEmpty) { "Recipe produced no output" }
                PatternDetailsHelper.encodeCraftingPattern(recipe, inputs, output, substitute, AE2PatternDefinition.flag(definition, "substituteFluids"))
            }
            "stonecutting" -> {
                check(recipe is StonecutterRecipe) { "Expected a stonecutting recipe" }
                val input = AE2PatternDefinition.item(definition["input"])
                val grid = SimpleContainer(input)
                check(recipe.matches(grid, level)) { "Input does not match recipe" }
                val output = recipe.assemble(grid, level.registryAccess())
                check(!output.isEmpty) { "Recipe produced no output" }
                PatternDetailsHelper.encodeStonecuttingPattern(recipe, AEItemKey.of(input), AEItemKey.of(output), substitute)
            }
            else -> {
                check(recipe is SmithingRecipe) { "Expected a smithing recipe" }
                val template = AE2PatternDefinition.item(definition["template"])
                val base = AE2PatternDefinition.item(definition["base"])
                val addition = AE2PatternDefinition.item(definition["addition"])
                val grid = SimpleContainer(template, base, addition)
                check(recipe.matches(grid, level)) { "Inputs do not match recipe" }
                val output = recipe.assemble(grid, level.registryAccess())
                check(!output.isEmpty) { "Recipe produced no output" }
                PatternDetailsHelper.encodeSmithingTablePattern(recipe, AEItemKey.of(template), AEItemKey.of(base), AEItemKey.of(addition), AEItemKey.of(output), substitute)
            }
        }
    }

    @LuaFunction(mainThread = true)
    fun encodeProcessingPattern(definition: Map<*, *>): MethodResult = encode(definition, "processing")

    @LuaFunction(mainThread = true)
    fun encodeCraftingPattern(definition: Map<*, *>): MethodResult = encode(definition, "crafting")

    @LuaFunction(mainThread = true)
    fun encodeStonecuttingPattern(definition: Map<*, *>): MethodResult = encode(definition, "stonecutting")

    @LuaFunction(mainThread = true)
    fun encodeSmithingTablePattern(definition: Map<*, *>): MethodResult = encode(definition, "smithing")
}
