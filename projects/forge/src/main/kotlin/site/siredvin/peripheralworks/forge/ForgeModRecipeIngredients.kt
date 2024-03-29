package site.siredvin.peripheralworks.forge

import dan200.computercraft.shared.ModRegistry.Items
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.common.Tags
import site.siredvin.peripheralworks.xplat.ModRecipeIngredients

object ForgeModRecipeIngredients : ModRecipeIngredients {
    override val enderModem: Ingredient
        get() = Ingredient.of(Items.WIRELESS_MODEM_ADVANCED.get())
    override val printer: Ingredient
        get() = Ingredient.of(Items.PRINTER.get())
    override val peripheralium: Ingredient
        get() = Ingredient.of(site.siredvin.peripheralium.common.setup.Items.PERIPHERALIUM_DUST.get())
    override val peripheraliumUpgrade: Ingredient
        get() = Ingredient.of(site.siredvin.peripheralium.common.setup.Items.PERIPHERALIUM_UPGRADE_TEMPLATE.get())
    override val netheriteIngot: Ingredient
        get() = Ingredient.of(Tags.Items.INGOTS_NETHERITE)
    override val emerald: Ingredient
        get() = Ingredient.of(Tags.Items.GEMS_EMERALD)
    override val diamond: Ingredient
        get() = Ingredient.of(Tags.Items.GEMS_DIAMOND)
    override val ironIngot: Ingredient
        get() = Ingredient.of(Tags.Items.INGOTS_IRON)
    override val anyCoal: Ingredient
        get() = Ingredient.of(net.minecraft.world.item.Items.COAL)
    override val peripheraliumBlock: Ingredient
        get() = Ingredient.of(site.siredvin.peripheralium.common.setup.Blocks.PERIPHERALIUM_BLOCK.get())
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
        get() = Ingredient.of(Tags.Items.DYES_BLUE)

    override val craftingTable: Ingredient
        get() = Ingredient.of(net.minecraft.world.item.Items.CRAFTING_TABLE)

    override val book: Ingredient
        get() = Ingredient.of(net.minecraft.world.item.Items.BOOK)

    override val bookshelf: Ingredient
        get() = Ingredient.of(Tags.Items.BOOKSHELVES)
}
