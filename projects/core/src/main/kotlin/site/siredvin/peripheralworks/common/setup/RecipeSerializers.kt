package site.siredvin.peripheralworks.common.setup

import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer
import site.siredvin.peripheralworks.common.recipes.StatueCloningRecipe
import site.siredvin.peripheralworks.utils.modId
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform

object RecipeSerializers {
    val STATUE_CLONING = PeripheralWorksPlatform.registerRecipeSerializer(
        modId("statue_cloning"),
        SimpleCraftingRecipeSerializer(::StatueCloningRecipe),
    )

    fun doSomething() {}
}
