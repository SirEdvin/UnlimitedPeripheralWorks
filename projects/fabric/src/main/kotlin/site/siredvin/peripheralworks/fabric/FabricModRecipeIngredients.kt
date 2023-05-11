package site.siredvin.peripheralworks.fabric

import dan200.computercraft.shared.ModRegistry.Items
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags
import net.minecraft.world.item.crafting.Ingredient
import site.siredvin.peripheralworks.xplat.ModRecipeIngredients

object FabricModRecipeIngredients: ModRecipeIngredients {
    override val enderModem: Ingredient
        get() = Ingredient.of(Items.WIRELESS_MODEM_ADVANCED.get())
    override val peripheralium: Ingredient
        get() = Ingredient.of(site.siredvin.peripheralium.common.setup.Items.PERIPHERALIUM_DUST.get())
    override val netheriteIngot: Ingredient
        get() = Ingredient.of(ConventionalItemTags.NETHERITE_INGOTS)
    override val emerald: Ingredient
        get() = Ingredient.of(ConventionalItemTags.EMERALDS)
    override val diamond: Ingredient
        get() = Ingredient.of(ConventionalItemTags.DIAMONDS)
}