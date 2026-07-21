package site.siredvin.peripheralworks.data

import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.data.recipes.SpecialRecipeBuilder
import net.minecraft.world.item.crafting.Ingredient
import site.siredvin.broccolium.modules.data.recipe.TweakedShapedRecipeBuilder
import site.siredvin.broccolium.modules.data.recipe.TweakedSmithingTransformRecipeBuilder
import site.siredvin.peripheralworks.common.recipes.*
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.xplat.ModBlocksReference
import site.siredvin.peripheralworks.xplat.ModRecipeIngredients
import java.util.concurrent.CompletableFuture

class ModRecipeProvider(output: PackOutput, registries: CompletableFuture<HolderLookup.Provider>) : RecipeProvider(output, registries) {
    override fun buildRecipes(consumer: RecipeOutput) {
        val ingredients = ModRecipeIngredients.get()

        TweakedShapedRecipeBuilder(Items.PERIPHERALIUM_HUB.get().defaultInstance)
            .define('D', ingredients.diamond)
            .define('E', ingredients.emerald)
            .define('P', ingredients.peripheralium)
            .define('M', ingredients.enderModem)
            .pattern("PDP")
            .pattern("EME")
            .pattern("PDP")
            .save(consumer)

        TweakedSmithingTransformRecipeBuilder(
            ingredients.peripheraliumUpgrade,
            Ingredient.of(Items.PERIPHERALIUM_HUB.get()),
            ingredients.netheriteIngot,
            Items.NETHERITE_PERIPHERALIUM_HUB.get().defaultInstance,
        ).save(consumer, Items.NETHERITE_PERIPHERALIUM_HUB.id.toString())

        TweakedShapedRecipeBuilder(Blocks.PERIPHERAL_CASING.get().asItem().defaultInstance)
            .define('B', ingredients.peripheraliumBlock)
            .define('C', Ingredient.of(net.minecraft.tags.ItemTags.COALS))
            .define('I', ingredients.ironIngot)
            .pattern("ICI")
            .pattern("CBC")
            .pattern("ICI")
            .save(consumer)

        TweakedShapedRecipeBuilder(Blocks.UNIVERSAL_SCANNER.get().asItem().defaultInstance)
            .define('O', ingredients.observer)
            .define('C', Ingredient.of(Blocks.PERIPHERAL_CASING.get().asItem()))
            .define('P', ingredients.peripheralium)
            .pattern("POP")
            .pattern(" C ")
            .pattern(" O ")
            .save(consumer)

        TweakedShapedRecipeBuilder(Blocks.ULTIMATE_SENSOR.get().asItem().defaultInstance)
            .define('O', ingredients.daylightDetector)
            .define('C', Ingredient.of(Blocks.PERIPHERAL_CASING.get().asItem()))
            .define('P', ingredients.peripheralium)
            .pattern("POP")
            .pattern(" C ")
            .pattern(" O ")
            .save(consumer)

        TweakedShapedRecipeBuilder(Blocks.ITEM_PEDESTAL.get().asItem().defaultInstance.copyWithCount(5))
            .define('S', ingredients.smoothStone)
            .define('_', ingredients.smoothStoneSlab)
            .pattern("___")
            .pattern(" S ")
            .pattern("_S_")
            .save(consumer)

        TweakedShapedRecipeBuilder(Blocks.MAP_PEDESTAL.get().asItem().defaultInstance)
            .define('O', Ingredient.of(Blocks.ITEM_PEDESTAL.get()))
            .define('P', ingredients.peripheralium)
            .define('C', ingredients.compass)
            .pattern("PCP")
            .pattern(" O ")
            .pattern(" P ")
            .save(consumer)

        TweakedShapedRecipeBuilder(Blocks.DISPLAY_PEDESTAL.get().asItem().defaultInstance.copyWithCount(5))
            .define('O', Ingredient.of(Blocks.ITEM_PEDESTAL.get()))
            .define('P', ingredients.peripheralium)
            .define('B', ingredients.smoothBasalt)
            .pattern("POP")
            .pattern("OBO")
            .pattern("POP")
            .save(consumer)

        TweakedShapedRecipeBuilder(Items.ULTIMATE_CONFIGURATOR.get().defaultInstance)
            .define('I', ingredients.peripheralium)
            .define('S', ingredients.stick)
            .define('D', ingredients.diamond)
            .pattern(" ID")
            .pattern(" SI")
            .pattern("S  ")
            .save(consumer)

        TweakedShapedRecipeBuilder(Blocks.REMOTE_OBSERVER.get().asItem().defaultInstance)
            .define('O', ingredients.observer)
            .define('c', Ingredient.of(Blocks.PERIPHERAL_CASING.get()))
            .define('d', ingredients.peripheralium)
            .define('i', ingredients.ironIngot)
            .pattern("ddd")
            .pattern("dOd")
            .pattern("ici")
            .save(consumer)

        TweakedShapedRecipeBuilder(Blocks.PERIPHERAL_PROXY.get().asItem().defaultInstance)
            .define('O', ingredients.enderModem)
            .define('c', Ingredient.of(Blocks.PERIPHERAL_CASING.get()))
            .define('d', ingredients.peripheralium)
            .define('i', ingredients.diamond)
            .pattern("ddd")
            .pattern("dOd")
            .pattern("ici")
            .save(consumer)

        TweakedShapedRecipeBuilder(Blocks.FLEXIBLE_REALITY_ANCHOR.get().asItem().defaultInstance.copyWithCount(32))
            .define('S', ingredients.peripheralium)
            .define('I', ingredients.ironIngot)
            .pattern("I I")
            .pattern(" S ")
            .pattern("I I")
            .save(consumer)

        TweakedShapedRecipeBuilder(Blocks.REALITY_FORGER.get().asItem().defaultInstance)
            .define('C', Ingredient.of(Blocks.PERIPHERAL_CASING.get().asItem()))
            .define('P', ingredients.printer)
            .define('D', ingredients.peripheralium)
            .define('B', ingredients.blueDye)
            .pattern("DPD")
            .pattern("BCB")
            .pattern(" D ")
            .save(consumer)

        TweakedShapedRecipeBuilder(Blocks.RECIPE_REGISTRY.get().asItem().defaultInstance)
            .define('T', ingredients.craftingTable)
            .define('B', ingredients.book)
            .define('C', Ingredient.of(Blocks.PERIPHERAL_CASING.get().asItem()))
            .define('D', ingredients.peripheralium)
            .pattern("DTD")
            .pattern("BCB")
            .pattern(" D ")
            .save(consumer)

        TweakedShapedRecipeBuilder(Blocks.INFORMATIVE_REGISTRY.get().asItem().defaultInstance)
            .define('T', ingredients.bookshelf)
            .define('B', ingredients.book)
            .define('C', Ingredient.of(Blocks.PERIPHERAL_CASING.get().asItem()))
            .define('D', ingredients.peripheralium)
            .pattern("DTD")
            .pattern("BCB")
            .pattern(" D ")
            .save(consumer)

        TweakedShapedRecipeBuilder(Blocks.FLEXIBLE_STATUE.get().asItem().defaultInstance.copyWithCount(16))
            .define('S', ingredients.smoothStone)
            .define('P', ingredients.peripheralium)
            .pattern("S S")
            .pattern(" P ")
            .pattern("S S")
            .save(consumer)

        TweakedShapedRecipeBuilder(Blocks.STATUE_WORKBENCH.get().asItem().defaultInstance)
            .define('S', Ingredient.of(Blocks.FLEXIBLE_STATUE.get()))
            .define('B', ingredients.smoothStone)
            .define('C', Ingredient.of(Blocks.PERIPHERAL_CASING.get().asItem()))
            .define('D', ingredients.peripheralium)
            .pattern("BSB")
            .pattern("SCS")
            .pattern("BDB")
            .save(consumer)

        TweakedShapedRecipeBuilder(Blocks.ENTITY_LINK.get().asItem().defaultInstance)
            .define('C', Ingredient.of(Items.ENTITY_CARD.get()))
            .define('P', Ingredient.of(Blocks.PERIPHERAL_CASING.get().asItem()))
            .define('O', ingredients.peripheralium)
            .define('D', ingredients.diamond)
            .pattern("COC")
            .pattern("DPD")
            .pattern("COC")
            .save(consumer)

        TweakedShapedRecipeBuilder(Items.ENTITY_CARD.get().asItem().defaultInstance)
            .define('D', ingredients.diamond)
            .define('O', ingredients.peripheralium)
            .define('B', ingredients.blackstone)
            .pattern("D O")
            .pattern(" B ")
            .pattern("D O")
            .save(consumer)

        TweakedShapedRecipeBuilder(Blocks.NETWORK_MANAGER.get().asItem().defaultInstance)
            .define('P', Ingredient.of(Blocks.PERIPHERAL_CASING.get().asItem()))
            .define('C', net.minecraft.world.item.Items.COBWEB)
            .define('K', ModBlocksReference.get().cable)
            .pattern("KCK")
            .pattern("CPC")
            .pattern("KCK")
            .save(consumer)

        TweakedShapedRecipeBuilder(Blocks.HOLOGRAM_PROJECTOR.get().asItem().defaultInstance)
            .define('R', net.minecraft.world.item.Items.RED_STAINED_GLASS)
            .define('B', net.minecraft.world.item.Items.BLUE_STAINED_GLASS)
            .define('C', Blocks.PERIPHERAL_CASING.get())
            .define('D', ingredients.peripheralium)
            .pattern("DBD")
            .pattern("RCR")
            .pattern("DRD")
            .save(consumer)

        SpecialRecipeBuilder.special(::StatueCloningRecipe)
            .save(consumer, "statue_cloning")
        SpecialRecipeBuilder.special(::StatueCleanRecipe)
            .save(consumer, "statue_clean")
        SpecialRecipeBuilder.special(::AnchorCloningRecipe)
            .save(consumer, "anchor_cloning")
        SpecialRecipeBuilder.special(::AnchorCleanRecipe)
            .save(consumer, "anchor_clean")
        SpecialRecipeBuilder.special(::CardCleanRecipe)
            .save(consumer, "card_clean")
    }
}
