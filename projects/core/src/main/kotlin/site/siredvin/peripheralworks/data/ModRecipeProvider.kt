package site.siredvin.peripheralworks.data

import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.world.item.crafting.Ingredient
import site.siredvin.peripheralium.data.blocks.TweakedShapedRecipeBuilder
import site.siredvin.peripheralium.data.blocks.TweakedUpgradeRecipeBuilder
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.xplat.ModRecipeIngredients
import java.util.function.Consumer

class ModRecipeProvider(output: PackOutput) : RecipeProvider(output) {
    override fun buildRecipes(consumer: Consumer<FinishedRecipe>) {
        val ingredients = ModRecipeIngredients.get()

        TweakedShapedRecipeBuilder.shaped(Items.PERIPHERALIUM_HUB.get())
            .define('D', ingredients.diamond)
            .define('E', ingredients.emerald)
            .define('P', ingredients.peripheralium)
            .define('M', ingredients.enderModem)
            .pattern("PDP")
            .pattern("EME")
            .pattern("PDP")
            .save(consumer)

        TweakedUpgradeRecipeBuilder.smithing(
            Ingredient.of(Items.PERIPHERALIUM_HUB.get()),
            ingredients.netheriteIngot,
            Items.NETHERITE_PERIPHERALIUM_HUB.get(),
        ).save(consumer)

        TweakedShapedRecipeBuilder.shaped(Blocks.PERIPHERAL_CASING.get().asItem())
            .define('B', ingredients.peripheraliumBlock)
            .define('C', ingredients.anyCoal)
            .define('I', ingredients.ironIngot)
            .pattern("ICI")
            .pattern("CBC")
            .pattern("ICI")
            .save(consumer)

        TweakedShapedRecipeBuilder.shaped(Blocks.UNIVERSAL_SCANNER.get().asItem())
            .define('O', ingredients.observer)
            .define('C', Ingredient.of(Blocks.PERIPHERAL_CASING.get().asItem()))
            .define('P', ingredients.peripheralium)
            .pattern("POP")
            .pattern(" C ")
            .pattern(" O ")
            .save(consumer)

        TweakedShapedRecipeBuilder.shaped(Blocks.ITEM_PEDESTAL.get().asItem(), 5)
            .define('S', ingredients.smoothStone)
            .define('_', ingredients.smoothStoneSlab)
            .pattern("___")
            .pattern(" S ")
            .pattern("_S_")
            .save(consumer)

        TweakedShapedRecipeBuilder.shaped(Blocks.MAP_PEDESTAL.get().asItem())
            .define('O', Ingredient.of(Blocks.ITEM_PEDESTAL.get()))
            .define('P', ingredients.peripheralium)
            .define('C', ingredients.compass)
            .pattern("PCP")
            .pattern(" O ")
            .pattern(" P ")
            .save(consumer)

        TweakedShapedRecipeBuilder.shaped(Blocks.DISPLAY_PEDESTAL.get().asItem(), 4)
            .define('O', Ingredient.of(Blocks.ITEM_PEDESTAL.get()))
            .define('P', ingredients.peripheralium)
            .define('B', ingredients.smoothBasalt)
            .pattern("POP")
            .pattern("OBO")
            .pattern("POP")
            .save(consumer)

        TweakedShapedRecipeBuilder.shaped(Items.ULTIMATE_CONFIGURATOR.get())
            .define('I', ingredients.peripheralium)
            .define('S', ingredients.stick)
            .define('D', ingredients.diamond)
            .pattern(" ID")
            .pattern(" SI")
            .pattern("S  ")
            .save(consumer)

        TweakedShapedRecipeBuilder.shaped(Blocks.REMOTE_OBSERVER.get())
            .define('O', ingredients.observer)
            .define('c', Ingredient.of(Blocks.PERIPHERAL_CASING.get()))
            .define('d', ingredients.peripheralium)
            .define('i', ingredients.ironIngot)
            .pattern("ddd")
            .pattern("dOd")
            .pattern("ici")
            .save(consumer)

        TweakedShapedRecipeBuilder.shaped(Blocks.PERIPHERAL_PROXY.get())
            .define('O', ingredients.enderModem)
            .define('c', Ingredient.of(Blocks.PERIPHERAL_CASING.get()))
            .define('d', ingredients.peripheralium)
            .define('i', ingredients.diamond)
            .pattern("ddd")
            .pattern("dOd")
            .pattern("ici")
            .save(consumer)
    }
}
