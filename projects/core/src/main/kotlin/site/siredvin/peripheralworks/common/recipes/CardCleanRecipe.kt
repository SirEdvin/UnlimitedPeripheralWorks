package site.siredvin.peripheralworks.common.recipes

import net.minecraft.core.RegistryAccess
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.CustomRecipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.Level
import site.siredvin.peripheralworks.common.item.EntityCard
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.common.setup.RecipeSerializers

class CardCleanRecipe(id: ResourceLocation, category: CraftingBookCategory) : CustomRecipe(id, category) {

    companion object {
        fun isFilledCreatureCard(stack: ItemStack): Boolean {
            return stack.`is`(Items.ENTITY_CARD.get()) && !EntityCard.isEmpty(stack)
        }
    }

    private val resultItem by lazy {
        Items.ENTITY_CARD.get().defaultInstance
    }

    override fun matches(p0: CraftingContainer, p1: Level): Boolean {
        return p0.items.count { !it.isEmpty } == 1 && p0.items.any(::isFilledCreatureCard)
    }

    override fun getResultItem(registry: RegistryAccess): ItemStack {
        return resultItem
    }

    override fun assemble(p0: CraftingContainer, p1: RegistryAccess): ItemStack {
        val firstCandidate = p0.items.find(::isFilledCreatureCard) ?: ItemStack.EMPTY
        return resultItem.copyWithCount(firstCandidate.count)
    }

    override fun canCraftInDimensions(p0: Int, p1: Int): Boolean {
        return true
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return RecipeSerializers.CARD_CLEAN.get()
    }
}
