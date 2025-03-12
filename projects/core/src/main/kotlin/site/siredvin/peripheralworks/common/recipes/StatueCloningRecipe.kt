package site.siredvin.peripheralworks.common.recipes

import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.CustomRecipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.Level
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.RecipeSerializers

class StatueCloningRecipe(category: CraftingBookCategory) : CustomRecipe(category) {

    companion object {
        private fun isSuitableStatue(stack: ItemStack): Boolean = stack.`is`(Blocks.FLEXIBLE_STATUE.get().asItem()) &&
            stack.get(
                DataComponents.CUSTOM_DATA,
            ) != null
    }

    override fun matches(p0: CraftingInput, p1: Level): Boolean {
        var possibleCandidate = ItemStack.EMPTY
        var copyCount = 0
        p0.items().forEach {
            if (isSuitableStatue(it)) {
                if (possibleCandidate.isEmpty) {
                    possibleCandidate = it
                } else {
                    return false
                }
            } else if (it.`is`(Blocks.FLEXIBLE_STATUE.get().asItem())) {
                copyCount += 1
            } else if (!it.isEmpty) {
                return false
            }
        }
        return !possibleCandidate.isEmpty && copyCount > 0
    }

    override fun assemble(p0: CraftingInput, p1: HolderLookup.Provider): ItemStack {
        val firstCandidate = p0.items().find(Companion::isSuitableStatue) ?: ItemStack.EMPTY
        val count = p0.items().count { it.`is`(Blocks.FLEXIBLE_STATUE.get().asItem()) }
        return firstCandidate.copyWithCount(count)
    }

    override fun canCraftInDimensions(p0: Int, p1: Int): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = RecipeSerializers.STATUE_CLONING.get()
}
