package site.siredvin.peripheralworks.forge

import dan200.computercraft.shared.ModRegistry.Items
import net.minecraft.world.item.crafting.Ingredient
import net.minecraftforge.common.Tags
import site.siredvin.peripheralworks.xplat.ModRecipeIngredients

object ForgeModRecipeIngredients: ModRecipeIngredients {
    override val enderModem: Ingredient
        get() = Ingredient.of(Items.WIRELESS_MODEM_ADVANCED.get())
    override val peripheralium: Ingredient
        get() = Ingredient.of(site.siredvin.peripheralium.common.setup.Items.PERIPHERALIUM_DUST.get())
    override val netheriteIngot: Ingredient
        get() = Ingredient.of(Tags.Items.INGOTS_NETHERITE)
    override val emerald: Ingredient
        get() = Ingredient.of(Tags.Items.GEMS_EMERALD)
    override val diamond: Ingredient
        get() = Ingredient.of(Tags.Items.GEMS_DIAMOND)
}