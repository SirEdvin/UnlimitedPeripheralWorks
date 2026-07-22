package site.siredvin.peripheralworks.client.configurator

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.common.configuration.ConfigHolder

object NetworkManagerClientSettings {
    const val MAX_EXPANDED_PATH_LENGTH = 256
    const val MAX_EXPANDED_PATHS = 256

    data class Settings(
        val expandedPaths: Set<String> = emptySet(),
    )

    private val values by lazy { load() }

    private fun key(dimension: ResourceLocation, pos: BlockPos) = "$dimension|${pos.asLong()}"

    fun get(dimension: ResourceLocation, pos: BlockPos): Settings = values[key(dimension, pos)] ?: Settings()

    fun set(dimension: ResourceLocation, pos: BlockPos, settings: Settings) {
        values[key(dimension, pos)] = settings.copy(
            expandedPaths = settings.expandedPaths.asSequence().filter { it.length <= MAX_EXPANDED_PATH_LENGTH }.take(MAX_EXPANDED_PATHS).toSet(),
        )
        ConfigHolder.clientConfig.networkManagerSettings.set(
            values.toSortedMap().map { (key, settings) ->
                JsonObject().apply {
                    addProperty("key", key)
                    add("expandedPaths", JsonArray().apply { settings.expandedPaths.sorted().forEach(::add) })
                }.toString()
            },
        )
        ConfigHolder.clientConfig.networkManagerSettings.save()
    }

    private fun load(): MutableMap<String, Settings> = ConfigHolder.clientConfig.networkManagerSettings.get().mapNotNull { entry ->
        try {
            val json = JsonParser.parseString(entry).asJsonObject
            val key = json.get("key")?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString ?: return@mapNotNull null
            val expanded = json.get("expandedPaths")?.takeIf { it.isJsonArray }?.asJsonArray
                ?.asSequence()
                ?.mapNotNull { it.takeIf { path -> path.isJsonPrimitive && path.asJsonPrimitive.isString }?.asString?.takeIf { path -> path.length <= MAX_EXPANDED_PATH_LENGTH } }
                ?.take(MAX_EXPANDED_PATHS)
                ?.toSet() ?: emptySet()
            key to Settings(expanded)
        } catch (_: Exception) {
            null
        }
    }.toMap(mutableMapOf())
}
