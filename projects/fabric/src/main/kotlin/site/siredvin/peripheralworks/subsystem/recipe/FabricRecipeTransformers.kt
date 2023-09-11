package site.siredvin.peripheralworks.subsystem.recipe

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import site.siredvin.peripheralium.util.representation.LuaRepresentation

object FabricRecipeTransformers {
    fun init() {
        RecipeRegistryToolkit.registerSerializer(FluidVariant::class.java) {
            LuaRepresentation.forFluid(it.fluid)
        }
        RecipeRegistryToolkit.registerSerializer(ItemVariant::class.java) {
            LuaRepresentation.forItem(it.item)
        }
    }
}
