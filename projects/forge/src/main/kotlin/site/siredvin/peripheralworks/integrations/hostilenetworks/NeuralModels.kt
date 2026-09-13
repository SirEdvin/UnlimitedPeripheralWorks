package site.siredvin.peripheralworks.integrations.hostilenetworks

import dan200.computercraft.api.lua.LuaException
import dev.shadowsoffire.hostilenetworks.data.DataModel
import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry
import dev.shadowsoffire.placebo.reload.DynamicHolder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

@Suppress("DEPRECATION")
object NeuralModels {
    fun entities(model: DataModel): List<String> = (listOf(model.type()) + model.subtypes())
        .filterNotNull().mapNotNull { BuiltInRegistries.ENTITY_TYPE.getKey(it)?.toString() }.distinct().sorted()

    fun entities(): List<String> = DataModelRegistry.INSTANCE.values.flatMap(::entities).distinct().sorted()

    fun resolve(entity: String): DynamicHolder<DataModel> {
        val id = if (entity.contains(':')) ResourceLocation.tryParse(entity) else null
        if (id == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) throw LuaException("Unknown or invalid entity ID: $entity")
        val type = BuiltInRegistries.ENTITY_TYPE.get(id)
        val models = DataModelRegistry.INSTANCE.values
        val primary = models.filter { it.type() == type }
        val matches = primary.ifEmpty { models.filter { type in it.subtypes() } }
        if (matches.isEmpty()) throw LuaException("No data model for entity: $entity")
        if (matches.size != 1) throw LuaException("Ambiguous data models for entity: $entity")
        return DataModelRegistry.INSTANCE.holder(matches.single())
    }

    fun identity(model: DataModel): Map<String, Any> = mapOf(
        "modelId" to DataModelRegistry.INSTANCE.getKey(model).toString(),
        "entityId" to BuiltInRegistries.ENTITY_TYPE.getKey(model.type()).toString(),
    )

    // Validate before native getters: ResourceLocation constructors and display-name code can throw on bad saves.
    fun storedModels(stack: ItemStack, combined: Boolean): List<DataModel>? {
        val tag = stack.getTagElement("data_model") ?: return null
        val ids = if (combined) {
            if (!tag.contains("ids", Tag.TAG_LIST.toInt())) return null
            val list = tag.getList("ids", Tag.TAG_STRING.toInt())
            if (list.size != 4) return null
            list.map { it.asString }
        } else {
            if (!tag.contains("id", Tag.TAG_STRING.toInt())) return null
            listOf(tag.getString("id"))
        }
        return ids.map { value ->
            val id = ResourceLocation.tryParse(value) ?: return null
            DataModelRegistry.INSTANCE.getValue(id) ?: return null
        }
    }

    fun validProgress(stack: ItemStack): Boolean {
        val tag = stack.getTagElement("data_model") ?: return false
        return listOf("data", "iterations").all { key ->
            !tag.contains(key) || (tag.contains(key, Tag.TAG_INT.toInt()) && tag.getInt(key) >= 0)
        }
    }

    fun progression(rank: String, data: Int, tierData: Int, nextTierData: Int?, dataPerKill: Int, cost: Int, iterations: Int): Map<String, Any> = buildMap {
        put("rank", rank)
        put("data", data)
        put("tierData", tierData)
        put("maxRank", nextTierData == null)
        if (nextTierData != null) {
            put("nextTierData", nextTierData)
            put("remainingData", (nextTierData.toLong() - data).coerceAtLeast(0))
        }
        put("dataPerKill", dataPerKill)
        put("simulationCost", cost)
        put("iterations", iterations)
    }
}
