package site.siredvin.peripheralworks.integrations.hostilenetworks

import dan200.computercraft.api.lua.LuaException
import dev.shadowsoffire.hostilenetworks.data.DataModel
import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry
import dev.shadowsoffire.hostilenetworks.data.EntityDataModel
import dev.shadowsoffire.hostilenetworks.item.DataModelItem
import dev.shadowsoffire.placebo.reload.DynamicHolder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

@Suppress("DEPRECATION")
object NeuralModels {
    fun entities(model: EntityDataModel): List<String> = (listOf(model.entity()) + model.variants())
        .filterNotNull().mapNotNull { BuiltInRegistries.ENTITY_TYPE.getKey(it)?.toString() }.distinct().sorted()

    fun entities(): List<String> = DataModelRegistry.INSTANCE.values.filterIsInstance<EntityDataModel>().flatMap(::entities).distinct().sorted()

    fun resolve(entity: String): DynamicHolder<DataModel> {
        val id = if (entity.contains(':')) ResourceLocation.tryParse(entity) else null
        if (id == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) throw LuaException("Unknown or invalid entity ID: $entity")
        val type = BuiltInRegistries.ENTITY_TYPE.get(id)
        val models = DataModelRegistry.INSTANCE.values.filterIsInstance<EntityDataModel>()
        val primary = models.filter { it.entity() == type }
        val matches = primary.ifEmpty { models.filter { type in it.variants() } }
        if (matches.isEmpty()) throw LuaException("No data model for entity: $entity")
        if (matches.size != 1) throw LuaException("Ambiguous data models for entity: $entity")
        return DataModelRegistry.INSTANCE.holder(matches.single())
    }

    fun identity(model: EntityDataModel): Map<String, Any> = mapOf(
        "modelId" to DataModelRegistry.INSTANCE.getKey(model).toString(),
        "entityId" to BuiltInRegistries.ENTITY_TYPE.getKey(model.entity()).toString(),
    )

    // Holders can become unbound after a reload. The entity-based API must not mislabel block models.
    fun storedModels(holders: List<DynamicHolder<DataModel>>, expectedCount: Int): List<EntityDataModel>? {
        if (holders.size != expectedCount) return null
        return holders.map { holder ->
            if (!holder.isBound) return null
            holder.get() as? EntityDataModel ?: return null
        }
    }

    fun validProgress(stack: ItemStack): Boolean = DataModelItem.getData(stack) >= 0 && DataModelItem.getIters(stack) >= 0

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
