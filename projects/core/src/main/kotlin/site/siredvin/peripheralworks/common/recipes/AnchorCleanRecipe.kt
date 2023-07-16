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

class AnchorCleanRecipe(id: ResourceLocation, category: CraftingBookCategory) : CustomRecipe(id, category) {

    companion object {
        private fun isSuitableAnchor(stack: ItemStack): Boolean {
            return stack.`is`(Blocks.FLEXIBLE_REALITY_ANCHOR.get().asItem()) && stack.getTagElement(BaseNBTBlock.INTERNAL_DATA_TAG) != null
        }
    }

    private val resultItem by lazy {
        Blocks.FLEXIBLE_REALITY_ANCHOR.get().asItem().defaultInstance
    }

    override fun matches(p0: CraftingContainer, p1: Level): Boolean {
        return p0.items.count { !it.isEmpty } == 1 && p0.items.any { isSuitableAnchor(it) }
    }

    override fun getResultItem(registry: RegistryAccess): ItemStack {
        return resultItem
    }

    override fun assemble(p0: CraftingContainer, p1: RegistryAccess): ItemStack {
        val firstCandidate = p0.items.find(Companion::isSuitableAnchor) ?: ItemStack.EMPTY
        return resultItem.copyWithCount(firstCandidate.count)
    }

    override fun canCraftInDimensions(p0: Int, p1: Int): Boolean {
        return true
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return RecipeSerializers.ANCHOR_CLEAN.get()
    }
}
