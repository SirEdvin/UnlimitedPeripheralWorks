package site.siredvin.peripheralworks.client.configurator

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object NetworkManagerClientSettings {
    const val DEFAULT_DELIMITER = "/"
    const val DEFAULT_RANGE = 32
    const val MIN_RANGE = 4
    const val MAX_RANGE = 128
    const val MAX_DELIMITER_LENGTH = 16
    const val MAX_EXPANDED_PATH_LENGTH = 256
    const val MAX_EXPANDED_PATHS = 256

    data class Settings(
        val delimiter: String = DEFAULT_DELIMITER,
        val range: Int = DEFAULT_RANGE,
        val expandedPaths: Set<String> = emptySet(),
    )

    private val file by lazy { Minecraft.getInstance().gameDirectory.toPath().resolve("config/peripheralworks-network-managers.json") }
    private val values by lazy { load() }

    private fun key(dimension: ResourceLocation, pos: BlockPos) = "$dimension|${pos.asLong()}"

    fun get(dimension: ResourceLocation, pos: BlockPos): Settings = values[key(dimension, pos)] ?: Settings()

    fun set(dimension: ResourceLocation, pos: BlockPos, settings: Settings) {
        values[key(dimension, pos)] = settings.copy(
            delimiter = settings.delimiter.take(MAX_DELIMITER_LENGTH),
            range = settings.range.coerceIn(MIN_RANGE, MAX_RANGE),
            expandedPaths = settings.expandedPaths.asSequence().filter { it.length <= MAX_EXPANDED_PATH_LENGTH }.take(MAX_EXPANDED_PATHS).toSet(),
        )
        save()
    }

    private fun load(): MutableMap<String, Settings> {
        if (!Files.isRegularFile(file)) return mutableMapOf()
        return try {
            val entries = Files.newBufferedReader(file).use { JsonParser.parseReader(it).asJsonObject.entrySet() }
            entries.mapNotNull { (key, value) ->
                val json = value.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
                val delimiter = json.get("delimiter")?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString?.take(MAX_DELIMITER_LENGTH) ?: DEFAULT_DELIMITER
                val range = json.get("range")?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }?.asInt?.coerceIn(MIN_RANGE, MAX_RANGE) ?: DEFAULT_RANGE
                val expanded = json.get("expandedPaths")?.takeIf { it.isJsonArray }?.asJsonArray
                    ?.asSequence()
                    ?.mapNotNull { it.takeIf { path -> path.isJsonPrimitive && path.asJsonPrimitive.isString }?.asString?.takeIf { path -> path.length <= MAX_EXPANDED_PATH_LENGTH } }
                    ?.take(MAX_EXPANDED_PATHS)
                    ?.toSet() ?: emptySet()
                key to Settings(delimiter, range, expanded)
            }.toMap(mutableMapOf())
        } catch (_: Exception) {
            mutableMapOf()
        }
    }

    private fun save() {
        try {
            Files.createDirectories(file.parent)
            val json = JsonObject()
            values.toSortedMap().forEach { (key, settings) ->
                json.add(
                    key,
                    JsonObject().apply {
                        addProperty("delimiter", settings.delimiter)
                        addProperty("range", settings.range)
                        add("expandedPaths", JsonArray().apply { settings.expandedPaths.sorted().forEach(::add) })
                    },
                )
            }
            val temporary = file.resolveSibling("${file.fileName}.tmp")
            Files.newBufferedWriter(temporary).use { GsonBuilder().setPrettyPrinting().create().toJson(json, it) }
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            } catch (_: Exception) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (_: Exception) {
            // Client presentation settings are optional; a read-only config directory must not break play.
        }
    }
}
