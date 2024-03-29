package site.siredvin.peripheralworks.fabric

import dan200.computercraft.shared.ModRegistry.Items
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.block.Blocks
import site.siredvin.peripheralworks.xplat.ModRecipeIngredients

object FabricModRecipeIngredients : ModRecipeIngredients {
    override val enderModem: Ingredient
        get() = Ingredient.of(Items.WIRELESS_MODEM_ADVANCED.get())
    override val printer: Ingredient
        get() = Ingredient.of(Items.PRINTER.get())
    override val peripheralium: Ingredient
        get() = Ingredient.of(site.siredvin.peripheralium.common.setup.Items.PERIPHERALIUM_DUST.get())

    override val peripheraliumUpgrade: Ingredient
        get() = Ingredient.of(site.siredvin.peripheralium.common.setup.Items.PERIPHERALIUM_UPGRADE_TEMPLATE.get())
    override val netheriteIngot: Ingredient
        get() = Ingredient.of(ConventionalItemTags.NETHERITE_INGOTS)
    override val emerald: Ingredient
        get() = Ingredient.of(ConventionalItemTags.EMERALDS)
    override val diamond: Ingredient
        get() = Ingredient.of(ConventionalItemTags.DIAMONDS)
    override val ironIngot: Ingredient
        get() = Ingredient.of(ConventionalItemTags.IRON_INGOTS)
    override val anyCoal: Ingredient
        get() = Ingredient.of(ConventionalItemTags.COAL)
    override val peripheraliumBlock: Ingredient
        get() = Ingredient.of(site.siredvin.peripheralium.common.setup.Blocks.PERIPHERALIUM_BLOCK.get().asItem())
    override val observer: Ingredient
        get() = Ingredient.of(Blocks.OBSERVER)
    override val smoothStone: Ingredient
        get() = Ingredient.of(Blocks.SMOOTH_STONE)

    override val smoothBasalt: Ingredient
        get() = Ingredient.of(Blocks.SMOOTH_BASALT)
    override val smoothStoneSlab: Ingredient
        get() = Ingredient.of(Blocks.SMOOTH_STONE_SLAB)
    override val compass: Ingredient
        get() = Ingredient.of(net.minecraft.world.item.Items.COMPASS)

    override val stick: Ingredient
        get() = Ingredient.of(net.minecraft.world.item.Items.STICK)

    override val blueDye: Ingredient
        get() = Ingredient.of(ConventionalItemTags.BLUE_DYES)

    override val craftingTable: Ingredient
        get() = Ingredient.of(net.minecraft.world.item.Items.CRAFTING_TABLE)

    override val book: Ingredient
        get() = Ingredient.of(net.minecraft.world.item.Items.BOOK)

    override val bookshelf: Ingredient
        get() = Ingredient.of(ConventionalItemTags.BOOKSHELVES)
}
