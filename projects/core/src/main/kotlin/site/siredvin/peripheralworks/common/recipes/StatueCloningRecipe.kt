package site.siredvin.peripheralworks.common.recipes

import net.minecraft.core.RegistryAccess
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.CustomRecipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.common.blocks.BaseNBTBlock
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.RecipeSerializers

class StatueCloningRecipe(id: ResourceLocation, category: CraftingBookCategory) : CustomRecipe(id, category) {

    companion object {
        private fun isSuitableStatue(stack: ItemStack): Boolean {
            return stack.`is`(Blocks.FLEXIBLE_STATUE.get().asItem()) && stack.getTagElement(BaseNBTBlock.INTERNAL_DATA_TAG) != null
        }
    }

    override fun matches(p0: CraftingContainer, p1: Level): Boolean {
        var possibleCandidate = ItemStack.EMPTY
        p0.items.forEach {
            if (isSuitableStatue(it)) {
                if (possibleCandidate.isEmpty) {
                    possibleCandidate = it
                } else {
                    return false
                }
            }
        }
        return !possibleCandidate.isEmpty
    }

    override fun assemble(p0: CraftingContainer, p1: RegistryAccess): ItemStack {
        val firstCandidate = p0.items.find(Companion::isSuitableStatue) ?: ItemStack.EMPTY
        val count = p0.items.count { it.`is`(Blocks.FLEXIBLE_STATUE.get().asItem()) }
        return firstCandidate.copyWithCount(count)
    }

    override fun canCraftInDimensions(p0: Int, p1: Int): Boolean {
        return true
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return RecipeSerializers.STATUE_CLONING.get()
    }
}