package site.siredvin.peripheralworks.subsystem.recipe

import net.minecraftforge.fluids.FluidStack
import site.siredvin.peripheralium.storages.fluid.toVanilla
import site.siredvin.peripheralium.util.representation.LuaRepresentation

object ForgeRecipeTransformers {
    fun init() {
        RecipeRegistryToolkit.registerSerializer(FluidStack::class.java) {
            LuaRepresentation.forFluidStack(it.toVanilla())
        }
    }
}
