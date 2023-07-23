package site.siredvin.peripheralworks.data

import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.data.recipes.SpecialRecipeBuilder
import net.minecraft.world.item.crafting.Ingredient
import site.siredvin.peripheralium.data.blocks.TweakedShapedRecipeBuilder
import site.siredvin.peripheralium.data.blocks.TweakedSmithingTransformRecipeBuilder
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.common.setup.RecipeSerializers
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

        TweakedSmithingTransformRecipeBuilder.smithingTransform(
            ingredients.peripheraliumUpgrade,
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

        TweakedShapedRecipeBuilder.shaped(Blocks.FLEXIBLE_REALITY_ANCHOR.get(), 32)
            .define('S', ingredients.peripheralium)
            .define('I', ingredients.ironIngot)
            .pattern("I I")
            .pattern(" S ")
            .pattern("I I")
            .save(consumer)

        TweakedShapedRecipeBuilder.shaped(Blocks.REALITY_FORGER.get())
            .define('C', Ingredient.of(Blocks.PERIPHERAL_CASING.get().asItem()))
            .define('P', ingredients.printer)
            .define('D', ingredients.peripheralium)
            .define('B', ingredients.blueDye)
            .pattern("DPD")
            .pattern("BCB")
            .pattern(" D ")
            .save(consumer)

        TweakedShapedRecipeBuilder.shaped(Blocks.RECIPE_REGISTRY.get())
            .define('T', ingredients.craftingTable)
            .define('B', ingredients.book)
            .define('C', Ingredient.of(Blocks.PERIPHERAL_CASING.get().asItem()))
            .define('D', ingredients.peripheralium)
            .pattern("DTD")
            .pattern("BCB")
            .pattern(" D ")
            .save(consumer)

        TweakedShapedRecipeBuilder.shaped(Blocks.INFORMATIVE_REGISTRY.get())
            .define('T', ingredients.bookshelf)
            .define('B', ingredients.book)
            .define('C', Ingredient.of(Blocks.PERIPHERAL_CASING.get().asItem()))
            .define('D', ingredients.peripheralium)
            .pattern("DTD")
            .pattern("BCB")
            .pattern(" D ")
            .save(consumer)

        TweakedShapedRecipeBuilder.shaped(Blocks.FLEXIBLE_STATUE.get(), 16)
            .define('S', ingredients.smoothStone)
            .define('P', ingredients.peripheralium)
            .pattern("S S")
            .pattern(" P ")
            .pattern("S S")
            .save(consumer)

        TweakedShapedRecipeBuilder.shaped(Blocks.STATUE_WORKBENCH.get())
            .define('S', Ingredient.of(Blocks.FLEXIBLE_STATUE.get()))
            .define('B', ingredients.smoothStone)
            .define('C', Ingredient.of(Blocks.PERIPHERAL_CASING.get().asItem()))
            .define('D', ingredients.peripheralium)
            .pattern("BSB")
            .pattern("SCS")
            .pattern("BDB")
            .save(consumer)

        SpecialRecipeBuilder.special(RecipeSerializers.STATUE_CLONING.get())
            .save(consumer, "statue_cloning")
        SpecialRecipeBuilder.special(RecipeSerializers.STATUE_CLEAN.get())
            .save(consumer, "statue_clean")
        SpecialRecipeBuilder.special(RecipeSerializers.ANCHOR_CLONING.get())
            .save(consumer, "anchor_cloning")
        SpecialRecipeBuilder.special(RecipeSerializers.ANCHOR_CLEAN.get())
            .save(consumer, "anchor_clean")
        SpecialRecipeBuilder.special(RecipeSerializers.CARD_CLEAN.get())
            .save(consumer, "card_clean")
    }
}
