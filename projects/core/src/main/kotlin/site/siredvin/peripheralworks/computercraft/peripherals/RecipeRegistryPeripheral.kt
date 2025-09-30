package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.Container
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType
import site.siredvin.broccolium.modules.platform.PlatformRegistries
import site.siredvin.peripheralworks.common.blockentity.RecipeRegistryBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.subsystem.recipe.RecipeRegistryToolkit
import site.siredvin.tweakium.modules.peripheral.OwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.ext.getResourceLocation
import site.siredvin.tweakium.modules.peripheral.owner.BlockEntityPeripheralOwner
import java.util.*
import java.util.stream.Collectors

class RecipeRegistryPeripheral(
    blockEntity: RecipeRegistryBlockEntity,
) : OwnedPeripheral<BlockEntityPeripheralOwner<RecipeRegistryBlockEntity>>(TYPE, BlockEntityPeripheralOwner(blockEntity)) {
    companion object {
        const val TYPE = "recipe_registry"
    }

    @Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
    val air = PlatformRegistries.ITEMS.get(ResourceLocation("minecraft", "air"))

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enableRecipeRegistry

    @LuaFunction
    @Throws(LuaException::class)
    fun getRecipeTypes(): MethodResult = MethodResult.of(
        PlatformRegistries.RECIPE_TYPES.keySet().stream().filter {
            !RecipeRegistryToolkit.excludedRecipeTypes.contains(it)
        }.map(ResourceLocation::toString)
            .collect(Collectors.toList()),
    )

    @LuaFunction
    @Throws(LuaException::class)
    fun getAllRecipesForType(arguments: IArguments): MethodResult {
        val recipeTypeID: ResourceLocation = arguments.getResourceLocation(0)

        @Suppress("UNCHECKED_CAST")
        val type = PlatformRegistries.RECIPE_TYPES.tryGet(recipeTypeID) as? RecipeType<Recipe<Container>> ?: return MethodResult.of(false, "Cannot find recipe type $recipeTypeID")
        return MethodResult.of(peripheralOwner.level!!.recipeManager.getAllRecipesFor(type).map { it.id.toString() })
    }

    @LuaFunction
    @Throws(LuaException::class)
    fun getRecipeForType(arguments: IArguments): MethodResult {
        val recipeTypeID: ResourceLocation = arguments.getResourceLocation(0)
        val recipeID: ResourceLocation = arguments.getResourceLocation(1)

        @Suppress("UNCHECKED_CAST")
        val type = PlatformRegistries.RECIPE_TYPES.tryGet(recipeTypeID) as? RecipeType<Recipe<Container>> ?: return MethodResult.of(false, "Cannot find recipe type $recipeTypeID")
        return MethodResult.of(
            peripheralOwner.level!!.recipeManager.getAllRecipesFor(type)
                .filter { it.id == recipeID }
                .map { RecipeRegistryToolkit.serializeRecipe(it, peripheralOwner.level!!.registryAccess()) }
                .toList(),
        )
    }

    @LuaFunction
    @Throws(LuaException::class)
    fun getRecipesFor(arguments: IArguments): MethodResult {
        val itemID: ResourceLocation = arguments.getResourceLocation(0)
        val types = arguments[1]
        val targetItem = PlatformRegistries.ITEMS.tryGet(itemID)

        if (targetItem == null || targetItem == air) {
            // Item registry may return AIR when the item is not found, and I hope no
            // one ever needs minecraft:air to be craftable...
            throw LuaException(String.format("Cannot find item with id %s", itemID))
        }

        val recipeTypes = RecipeRegistryToolkit.collectRecipeTypes(types)
        return MethodResult.of(
            recipeTypes.flatMap {
                @Suppress("UNCHECKED_CAST")
                peripheralOwner.level!!.recipeManager.getAllRecipesFor(it as RecipeType<Recipe<Container>>).stream().filter { recipe ->
                    recipe.getResultItem(peripheralOwner.level!!.registryAccess()).`is`(targetItem)
                }.toList()
            }.map { RecipeRegistryToolkit.serializeRecipe(it, peripheralOwner.level!!.registryAccess()) },
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RecipeRegistryPeripheral) return false
        if (!super.equals(other)) return false

        if (isEnabled != other.isEnabled) return false
        if (peripheralOwner != other.peripheralOwner) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + isEnabled.hashCode()
        result = 31 * result + peripheralOwner.hashCode()
        return result
    }
}
