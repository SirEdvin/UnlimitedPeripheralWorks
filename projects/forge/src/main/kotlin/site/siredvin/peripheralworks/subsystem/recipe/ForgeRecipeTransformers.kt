package site.siredvin.peripheralworks.subsystem.recipe

import net.neoforged.neoforge.fluids.FluidStack
import site.siredvin.broccolium.modules.storage.fluid.toVanilla
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation

object ForgeRecipeTransformers {
    fun init() {
        RecipeRegistryToolkit.registerSerializer(FluidStack::class.java) {
            LuaRepresentation.forFluidStack(it.toVanilla())
        }
    }
}
