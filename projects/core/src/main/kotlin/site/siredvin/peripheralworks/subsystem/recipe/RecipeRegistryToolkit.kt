package site.siredvin.peripheralworks.subsystem.recipe

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import dan200.computercraft.api.lua.LuaException
import net.minecraft.core.RegistryAccess
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.Container
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.world.level.material.Fluid
import site.siredvin.peripheralium.util.representation.LuaRepresentation
import site.siredvin.peripheralium.xplat.PeripheraliumPlatform
import site.siredvin.peripheralium.xplat.XplatRegistries
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.HashMap

object RecipeRegistryToolkit {
    private val GSON = Gson()
    val SERIALIZATION_NULL = Any()

    private val RECIPE_SERIALIZERS: MutableMap<Class<out Recipe<*>>, RecipeTransformer<*, Recipe<*>>> = mutableMapOf()
    private val SERIALIZERS: MutableMap<Class<*>, java.util.function.Function<Any, Any?>> = mutableMapOf()
    private val RECIPE_PREDICATES: MutableMap<RecipeType<*>, RecipeSearchPredicate> = mutableMapOf()
    private val EXCLUDED_RECIPE_TYPES: MutableSet<ResourceLocation> = mutableSetOf()

    val excludedRecipeTypes: Set<ResourceLocation>
        get() = EXCLUDED_RECIPE_TYPES

    private val DEFAULT_RECIPE_PREDICATE: RecipeSearchPredicate =
        RecipeSearchPredicate { stack, recipe, checkMode ->
            checkMode.itemStackEquals(
                recipe.getResultItem(RegistryAccess.EMPTY),
                stack,
            )
        }

    fun excludeRecipeType(type: ResourceLocation) {
        EXCLUDED_RECIPE_TYPES.add(type)
    }

    init {
        registerSerializer(Ingredient::class.java) {
            if (it.isEmpty) {
                return@registerSerializer SERIALIZATION_NULL
            }
            try {
                return@registerSerializer GSON.fromJson(it.toJson(), HashMap::class.java)
            } catch (ignored: JsonSyntaxException) {
                try {
                    return@registerSerializer GSON.fromJson(it.toJson(), ArrayList::class.java)
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                }
            }
            return@registerSerializer null
        }
        registerSerializer(ItemStack::class.java) {
            if (it.isEmpty) return@registerSerializer SERIALIZATION_NULL
            return@registerSerializer LuaRepresentation.forItemStack(it)
        }
        registerSerializer(Item::class.java, LuaRepresentation::forItem)
        registerSerializer(Fluid::class.java, LuaRepresentation::forFluid)
        registerSerializer(JsonObject::class.java, RecipeRegistryToolkit::serializeJson)
        registerSerializer(EntityType::class.java) {
            return@registerSerializer mutableMapOf(
                "name" to XplatRegistries.ENTITY_TYPES.getKey(it).toString(),
            )
        }
        registerSerializer(Tag::class.java, PeripheraliumPlatform::nbtToLua)
    }

    fun <V : Container, T : Recipe<V>> registerRecipeSerializer(recipeClass: Class<T>, transformer: RecipeTransformer<V, T>) {
        RECIPE_SERIALIZERS[recipeClass] = transformer as RecipeTransformer<*, Recipe<*>>
    }

    fun <V : Container, T : Recipe<V>> registerRecipeSerializerRaw(recipeClass: Class<T>, transformer: RecipeTransformer<Container, Recipe<Container>>) {
        RECIPE_SERIALIZERS[recipeClass] = transformer as RecipeTransformer<*, Recipe<*>>
    }

    fun <T> registerSerializer(clazz: Class<T>, serializer: java.util.function.Function<T, Any?>) {
        SERIALIZERS[clazz] = serializer as java.util.function.Function<Any, Any?>
    }

    fun <T : Recipe<*>> registerRecipePredicate(
        recipeType: RecipeType<T>,
        searchFunction: RecipeSearchPredicate,
    ) {
        RECIPE_PREDICATES[recipeType] = searchFunction
    }

    fun serializeJson(obj: JsonObject?): Any? {
        return GSON.fromJson(obj, HashMap::class.java)
    }

    fun serializePossibleCollection(obj: Any?): Any? {
        if (obj is Collection<*>) {
            return obj.stream().map<Any>(RecipeRegistryToolkit::serialize).collect(
                Collectors.toList(),
            )
        }
        if (obj != null && obj.javaClass.isArray) {
            return Arrays.stream(obj as Array<Any>).map(RecipeRegistryToolkit::serialize).collect(
                Collectors.toList(),
            )
        }
        return serialize(obj)
    }

    fun serialize(obj: Any?): Any? {
        if (obj is RecipeSerializableRecord) return obj.serializeForToolkit()
        for (clazz in SERIALIZERS.keys) {
            if (clazz.isInstance(obj)) return SERIALIZERS[clazz]!!.apply(clazz.cast(obj))
        }
        return obj
    }

    fun serializeRecipe(recipe: Recipe<*>): Map<String, Any> {
        for (recipeClass in RECIPE_SERIALIZERS.keys) {
            if (recipeClass.isInstance(recipe)) return RECIPE_SERIALIZERS[recipeClass]!!.transform(recipe as Recipe<Container>)
        }
        return DefaultRecipeTransformer.transform(recipe as Recipe<Container>)
    }

    @Throws(LuaException::class)
    fun getRecipeType(type: ResourceLocation): RecipeType<*> {
        return XplatRegistries.RECIPE_TYPES.tryGet(type)
            ?: throw LuaException(String.format("Incorrect recipe type %s", type))
    }

    fun getRecipesForType(recipeType: RecipeType<*>, level: Level): List<Recipe<*>> {
        return level.recipeManager.getAllRecipesFor(recipeType as RecipeType<Recipe<Container>>)
    }

    fun findRecipesForType(
        recipeType: RecipeType<*>,
        result: ItemStack,
        level: Level,
        checkMode: NBTCheckMode,
    ): MutableList<Any>? {
        val searchPredicate =
            RECIPE_PREDICATES.getOrDefault(recipeType, DEFAULT_RECIPE_PREDICATE)
        val recipes: List<Recipe<*>> = getRecipesForType(recipeType, level)
        return recipes.stream().filter {
            searchPredicate.test(
                result,
                it as Recipe<Container>,
                checkMode,
            )
        }.collect(Collectors.toList())
    }

    @Throws(LuaException::class)
    fun collectRecipeTypes(types: Any?): List<RecipeType<*>> {
        if (types == null || types.toString() == "*") return XplatRegistries.RECIPE_TYPES.iterator().asSequence().toList()
        if (types is String) {
            return if (types.contains(":")) {
                listOf(getRecipeType(ResourceLocation(types.toString())))
            } else {
                XplatRegistries.RECIPE_TYPES.iterator().asSequence()
                    .filter { p -> p.toString().startsWith(types) }.toList()
            }
        }
        if (types is Map<*, *>) {
            val recipeTypes: MutableList<RecipeType<*>> = mutableListOf()
            for (el in types.values) {
                recipeTypes.add(getRecipeType(ResourceLocation(el.toString())))
            }
            return recipeTypes
        }
        throw LuaException("types should be string or table!")
    }
}
