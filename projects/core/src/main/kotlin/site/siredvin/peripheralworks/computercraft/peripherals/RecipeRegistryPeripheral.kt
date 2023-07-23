package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.core.RegistryAccess
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.Container
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType
import site.siredvin.peripheralium.computercraft.peripheral.OwnedPeripheral
import site.siredvin.peripheralium.computercraft.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.peripheralium.ext.getResourceLocation
import site.siredvin.peripheralium.xplat.XplatRegistries
import site.siredvin.peripheralworks.common.blockentity.RecipeRegistryBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.subsystem.recipe.RecipeRegistryToolkit
import java.util.*
import java.util.stream.Collectors

class RecipeRegistryPeripheral(
    blockEntity: RecipeRegistryBlockEntity,
) : OwnedPeripheral<BlockEntityPeripheralOwner<RecipeRegistryBlockEntity>>(TYPE, BlockEntityPeripheralOwner(blockEntity)) {
    companion object {
        const val TYPE = "recipe_registry"
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enableRecipeRegistry

    @LuaFunction
    @Throws(LuaException::class)
    fun getRecipeTypes(): MethodResult {
        return MethodResult.of(
            XplatRegistries.RECIPE_TYPES.keySet().stream().filter {
                !RecipeRegistryToolkit.excludedRecipeTypes.contains(it)
            }.map(ResourceLocation::toString)
                .collect(Collectors.toList()),
        )
    }

    @LuaFunction
    @Throws(LuaException::class)
    fun getAllRecipesForType(arguments: IArguments): MethodResult {
        val recipeTypeID: ResourceLocation = arguments.getResourceLocation(0)

        @Suppress("UNCHECKED_CAST")
        val type = XplatRegistries.RECIPE_TYPES.tryGet(recipeTypeID) as? RecipeType<Recipe<Container>> ?: return MethodResult.of(false, "Cannot find recipe type $recipeTypeID")
        return MethodResult.of(level!!.recipeManager.getAllRecipesFor(type).map { it.id })
    }

    @LuaFunction
    @Throws(LuaException::class)
    fun getRecipeForType(arguments: IArguments): MethodResult {
        val recipeTypeID: ResourceLocation = arguments.getResourceLocation(0)
        val recipeID: ResourceLocation = arguments.getResourceLocation(1)

        @Suppress("UNCHECKED_CAST")
        val type = XplatRegistries.RECIPE_TYPES.tryGet(recipeTypeID) as? RecipeType<Recipe<Container>> ?: return MethodResult.of(false, "Cannot find recipe type $recipeTypeID")
        return MethodResult.of(level!!.recipeManager.getAllRecipesFor(type).filter { it.id == recipeID }.map(RecipeRegistryToolkit::serializeRecipe).toList())
    }

    @LuaFunction
    @Throws(LuaException::class)
    fun getRecipesFor(arguments: IArguments): MethodResult {
        val itemID: ResourceLocation = arguments.getResourceLocation(0)
        val types = arguments[1]
        val targetItem = XplatRegistries.ITEMS.tryGet(itemID) ?: throw LuaException(String.format("Cannot find item with id %s", itemID))
        val recipeTypes = RecipeRegistryToolkit.collectRecipeTypes(types)
        return MethodResult.of(
            recipeTypes.flatMap {
                @Suppress("UNCHECKED_CAST")
                level!!.recipeManager.getAllRecipesFor(it as RecipeType<Recipe<Container>>).stream().filter { recipe ->
                    recipe.getResultItem(RegistryAccess.EMPTY).`is`(targetItem)
                }.toList()
            },
        )
    }
}
