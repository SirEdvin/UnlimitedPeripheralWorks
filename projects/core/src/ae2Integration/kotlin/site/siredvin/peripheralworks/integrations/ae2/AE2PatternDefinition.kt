package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.stacks.AEFluidKey
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.GenericStack
import com.mojang.brigadier.exceptions.CommandSyntaxException
import dan200.computercraft.api.lua.LuaException
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.TagParser
import net.minecraft.world.item.ItemStack
import site.siredvin.broccolium.modules.platform.PlatformToolkit

internal class AE2PatternDefinition(registries: HolderLookup.Provider) {
    private val ops = registries.createSerializationContext(NbtOps.INSTANCE)
    companion object {
        private const val MAX_SAFE_INTEGER = 9_007_199_254_740_991L
    }

    fun table(value: Any?, name: String): Map<*, *> = value as? Map<*, *> ?: throw LuaException("$name must be a table")

    fun fields(table: Map<*, *>, vararg allowed: String) {
        if (table.keys.any { it !in allowed }) throw LuaException("Allowed fields: ${allowed.joinToString()}")
    }

    fun flag(table: Map<*, *>, name: String): Boolean = when (val value = table[name]) {
        null -> false
        is Boolean -> value
        else -> throw LuaException("$name must be a boolean")
    }

    fun slots(value: Any?, name: String, limit: Int, sparse: Boolean = false): Map<Int, Map<*, *>> {
        val table = table(value, name)
        if (table.isEmpty() || table.size > limit) throw LuaException("$name must contain 1 to $limit entries")
        val result = table.entries.associate { (key, entry) ->
            val index = (key as? Number)?.toDouble() ?: throw LuaException("$name requires numeric slot keys")
            if (!index.isFinite() || index % 1.0 != 0.0 || index < 1 || index > limit) throw LuaException("$name slots must be integers from 1 to $limit")
            index.toInt() to table(entry, "$name[$index]")
        }
        if (!sparse && result.keys != (1..result.size).toSet()) throw LuaException("$name must be a dense array")
        return result.toSortedMap()
    }

    fun resource(value: Any?): GenericStack {
        val table = table(value, "resource")
        fields(table, "type", "name", "count", "snbt")
        val count = (table["count"] as? Number)?.toDouble() ?: throw LuaException("count must be a positive integer")
        if (!count.isFinite() || count < 1 || count % 1.0 != 0.0 || count > MAX_SAFE_INTEGER.toDouble()) throw LuaException("count must be a positive safe integer")
        val basic = AE2Helper.parseResource(table, true)
        val components = table["snbt"]?.let {
            val text = it as? String ?: throw LuaException("snbt must be a compound SNBT string")
            DataComponentPatch.CODEC.parse(ops, parseTag(text)).getOrThrow { LuaException("Invalid resource components: $it") }
        } ?: DataComponentPatch.EMPTY
        val key = when (val key = basic.what) {
            is AEItemKey -> {
                AEItemKey.of(key.toStack().apply { applyComponents(components) })
            }
            is AEFluidKey -> AEFluidKey.of(key.toStack(1).apply { applyComponents(components) })
            else -> throw LuaException("Unsupported resource type")
        }
        return GenericStack(key, basic.amount)
    }

    fun item(value: Any?): ItemStack {
        val stack = resource(value)
        val key = stack.what as? AEItemKey ?: throw LuaException("Recipe ingredients must be items")
        if (stack.amount != 1L) throw LuaException("Recipe ingredients must have count 1")
        return key.toStack()
    }

    fun describe(stack: GenericStack): Map<String, Any> {
        val key = stack.what
        val divisor = if (key is AEFluidKey) PlatformToolkit.get().fluidCompactDivider.toLong() else 1L
        if (stack.amount <= 0 || stack.amount % divisor != 0L || stack.amount / divisor > MAX_SAFE_INTEGER) throw LuaException("Pattern amount cannot be represented exactly")

        return (AE2Helper.keyToMap(key) + ("count" to stack.amount / divisor)).toMutableMap().apply {
            val components = when (key) {
                is AEItemKey -> key.toStack().componentsPatch
                is AEFluidKey -> key.toStack(1).componentsPatch
                else -> throw LuaException("Unsupported resource type")
            }
            if (!components.isEmpty) {
                val text = DataComponentPatch.CODEC.encodeStart(ops, components).getOrThrow { LuaException("Cannot serialize resource components: $it") }.toString()
                checkTagBounds(text)
                put("snbt", text)
            }
        }
    }

    private fun parseTag(text: String): CompoundTag {
        checkTagBounds(text)
        return try {
            TagParser.parseTag(text)
        } catch (_: CommandSyntaxException) {
            throw LuaException("snbt must be a valid compound SNBT string")
        }
    }

    private fun checkTagBounds(text: String) {
        if (text.length > 65536 || text.toByteArray(Charsets.UTF_8).size > 65536) throw LuaException("snbt exceeds 65536 bytes")
        var quote: Char? = null
        var escaped = false
        var depth = 0
        for (char in text) {
            if (quote != null) {
                if (escaped) {
                    escaped = false
                } else if (char == '\\') {
                    escaped = true
                } else if (char == quote) {
                    quote = null
                }
            } else {
                when (char) {
                    '\'', '"' -> quote = char
                    '{', '[' -> {
                        depth++
                        if (depth > 64) throw LuaException("snbt exceeds nesting depth 64")
                    }
                    '}', ']' -> depth--
                }
            }
        }
    }
}
