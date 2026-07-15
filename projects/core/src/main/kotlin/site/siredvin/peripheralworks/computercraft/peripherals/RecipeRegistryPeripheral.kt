package site.siredvin.peripheralworks.computercraft.peripherals

import com.google.gson.JsonParser
import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.shared.util.NBTUtil
import net.minecraft.nbt.TagParser
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeInput
import net.minecraft.world.item.crafting.RecipeType
import site.siredvin.broccolium.modules.platform.PlatformRegistries
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.peripheralworks.common.blockentity.RecipeRegistryBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.subsystem.recipe.RecipeRegistryToolkit
import site.siredvin.peripheralworks.utils.toEntry
import site.siredvin.tweakium.modules.peripheral.OwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import site.siredvin.tweakium.modules.peripheral.ext.getResourceLocation
import site.siredvin.tweakium.modules.peripheral.owner.BlockEntityPeripheralOwner
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors
import kotlin.jvm.optionals.getOrNull

class RecipeRegistryPeripheral(
    blockEntity: RecipeRegistryBlockEntity,
) : OwnedPeripheral<BlockEntityPeripheralOwner<RecipeRegistryBlockEntity>>(TYPE, BlockEntityPeripheralOwner(blockEntity)) {
    companion object {
        const val TYPE = "recipe_registry"
        private val EXTRA_PLUGINS: MutableList<IPeripheralPlugin> = mutableListOf()
        fun addPlugin(plugin: IPeripheralPlugin) {
            EXTRA_PLUGINS.add(plugin)
        }
    }

    val air: Item = PlatformRegistries.ITEMS.get(ResourceLocation.withDefaultNamespace("air"))

    init {
        EXTRA_PLUGINS.forEach { addPlugin(it) }
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enableRecipeRegistry

    override val peripheralConfiguration: MutableMap<String, Any>
        get() {
            val base = super.peripheralConfiguration
            base["apiVersion"] = listOf(2, 0)
            return base
        }

    @LuaFunction
    @Throws(LuaException::class)
    fun getTypes(): MethodResult = MethodResult.of(
        PlatformRegistries.RECIPE_TYPES.keySet().stream().filter {
            !RecipeRegistryToolkit.excludedRecipeTypes.contains(it)
        }.map(ResourceLocation::toString)
            .collect(Collectors.toList()),
    )

    @LuaFunction
    fun getRecipeTypes(): MethodResult = getTypes()

    @LuaFunction
    @Throws(LuaException::class)
    fun getAllRecipesForType(arguments: IArguments): MethodResult {
        val recipeTypeID: ResourceLocation = arguments.getResourceLocation(0)

        @Suppress("UNCHECKED_CAST")
        val type = PlatformRegistries.RECIPE_TYPES.tryGet(recipeTypeID) as? RecipeType<Recipe<RecipeInput>> ?: return MethodResult.of(false, "Cannot find recipe type $recipeTypeID")
        return MethodResult.of(peripheralOwner.level!!.recipeManager.getAllRecipesFor(type).map { it.id.toString() })
    }

    @LuaFunction
    @Throws(LuaException::class)
    fun get(recipeID: String): MethodResult {
        val id = ResourceLocation.tryParse(recipeID) ?: return MethodResult.of(null)
        val recipe = peripheralOwner.level!!.recipeManager.byKey(id).getOrNull() ?: return MethodResult.of(null)
        return MethodResult.of(RecipeRegistryToolkit.serializeRecipe(recipe.toEntry(), peripheralOwner.level!!.registryAccess()))
    }

    @LuaFunction
    @Throws(LuaException::class)
    fun getRecipeForType(arguments: IArguments): MethodResult {
        val recipeTypeID = arguments.getResourceLocation(0)
        val recipeID = arguments.getResourceLocation(1)

        @Suppress("UNCHECKED_CAST")
        val type = PlatformRegistries.RECIPE_TYPES.tryGet(recipeTypeID) as? RecipeType<Recipe<RecipeInput>> ?: return MethodResult.of(false, "Cannot find recipe type $recipeTypeID")
        return MethodResult.of(
            peripheralOwner.level!!.recipeManager.getAllRecipesFor(type)
                .filter { it.id == recipeID }
                .map { RecipeRegistryToolkit.serializeRecipe(it.toEntry(), peripheralOwner.level!!.registryAccess()) },
        )
    }

    @LuaFunction
    fun getRaw(recipeID: String): MethodResult {
        val recipeId = ResourceLocation.tryParse(recipeID) ?: return MethodResult.of(null)

        val recipePath = ResourceLocation.fromNamespaceAndPath(
            recipeId.namespace,
            "recipe/" + recipeId.path + ".json",
        )
        val resourceManager = PlatformToolkit.get().minecraftServer?.resourceManager ?: return MethodResult.of(null, "Cannot find server variable")
        try {
            val resource = resourceManager.getResource(recipePath).orElseThrow()
            val reader = BufferedReader(
                InputStreamReader(resource.open()),
            )

            val jsonObject = JsonParser.parseReader(reader).getAsJsonObject()
            // Now you have the JSON content
            reader.close()
            val tag = TagParser.parseTag(jsonObject.toString())
            return MethodResult.of(NBTUtil.toLua(tag))
        } catch (e: Exception) {
            return MethodResult.of(null, e.toString())
        }
    }

    @LuaFunction
    @Throws(LuaException::class)
    fun list(arguments: IArguments): MethodResult {
        val types = arguments[0]
        val recipeTypes = RecipeRegistryToolkit.collectRecipeTypes(types).toSet()
        return MethodResult.of(peripheralOwner.level!!.recipeManager.recipes.filter { recipeTypes.contains(it.value.type) }.map { it.id.toString() })
    }

    @LuaFunction
    @Throws(LuaException::class)
    fun getRecipesFor(arguments: IArguments): MethodResult = search(arguments)

    @LuaFunction
    @Throws(LuaException::class)
    fun search(arguments: IArguments): MethodResult {
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
                peripheralOwner.level!!.recipeManager.getAllRecipesFor(it as RecipeType<Recipe<RecipeInput>>).stream().filter { recipe ->
                    recipe.value.getResultItem(peripheralOwner.level!!.registryAccess()).`is`(targetItem)
                }.toList()
            }.map { RecipeRegistryToolkit.serializeRecipe(it.toEntry(), peripheralOwner.level!!.registryAccess()) },
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
