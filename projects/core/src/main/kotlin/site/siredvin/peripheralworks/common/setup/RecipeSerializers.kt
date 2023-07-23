package site.siredvin.peripheralworks.common.setup

import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer
import site.siredvin.peripheralworks.common.recipes.*
import site.siredvin.peripheralworks.utils.modId
import site.siredvin.peripheralworks.xplat.ModPlatform

object RecipeSerializers {
    val STATUE_CLONING = ModPlatform.registerRecipeSerializer(
        modId("statue_cloning"),
        SimpleCraftingRecipeSerializer(::StatueCloningRecipe),
    )
    val STATUE_CLEAN = ModPlatform.registerRecipeSerializer(
        modId("statue_clean"),
        SimpleCraftingRecipeSerializer(::StatueCleanRecipe),
    )
    val ANCHOR_CLEAN = ModPlatform.registerRecipeSerializer(
        modId("anchor_clean"),
        SimpleCraftingRecipeSerializer(::AnchorCleanRecipe),
    )
    val ANCHOR_CLONING = ModPlatform.registerRecipeSerializer(
        modId("anchor_cloning"),
        SimpleCraftingRecipeSerializer(::AnchorCloningRecipe),
    )
    val CARD_CLEAN = ModPlatform.registerRecipeSerializer(
        modId("card_clean"),
        SimpleCraftingRecipeSerializer(::CardCleanRecipe),
    )

    fun doSomething() {}
}
