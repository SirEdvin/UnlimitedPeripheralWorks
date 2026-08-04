package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.stacks.*
import dan200.computercraft.api.lua.LuaException
import net.minecraft.resources.ResourceLocation
import site.siredvin.broccolium.modules.platform.PlatformRegistries
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation
import java.util.function.Predicate

object AE2Helper {

    private val ALWAYS: Predicate<AEKey> = Predicate { true }

    fun genericStackToMap(stack: GenericStack): MutableMap<String, Any> {
        if (stack.what is AEItemKey) {
            val base = LuaRepresentation.forItemStack((stack.what as AEItemKey).toStack(stack.amount.toInt()))
            base["type"] = "item"
            return base
        }
        val base = mutableMapOf<String, Any>()
        base["type"] = "fluid"
        base["name"] = PlatformRegistries.FLUIDS.getKey((stack.what as AEFluidKey).fluid).toString()
        base["count"] = stack.amount.toDouble() / PlatformToolkit.get().fluidCompactDivider
        return base
    }

    fun keyToMap(key: AEKey): Map<String, String> = when (key) {
        is AEItemKey -> mapOf("type" to "item", "name" to PlatformRegistries.ITEMS.getKey(key.item).toString())
        is AEFluidKey -> mapOf("type" to "fluid", "name" to PlatformRegistries.FLUIDS.getKey(key.fluid).toString())
        else -> throw LuaException("Unsupported AE2 resource type")
    }

    fun stackToMap(stack: GenericStack): Map<String, Any> = keyToMap(stack.what) + ("count" to publicAmount(stack.what, stack.amount))

    fun parseResource(resource: Map<*, *>, requireCount: Boolean): GenericStack {
        val type = resource["type"] as? String ?: throw LuaException("Resource type must be 'item' or 'fluid'")
        val name = resource["name"] as? String ?: throw LuaException("Resource name must be a registry ID")
        val id = ResourceLocation.tryParse(name) ?: throw LuaException("Invalid resource ID '$name'")
        val key = when (type) {
            "item" -> {
                if (id !in PlatformRegistries.ITEMS.keySet()) throw LuaException("Unknown item '$name'")
                val item = PlatformRegistries.ITEMS.get(id)
                AEItemKey.of(item)
            }
            "fluid" -> {
                if (id !in PlatformRegistries.FLUIDS.keySet()) throw LuaException("Unknown fluid '$name'")
                val fluid = PlatformRegistries.FLUIDS.get(id)
                AEFluidKey.of(fluid)
            }
            else -> throw LuaException("Resource type must be 'item' or 'fluid'")
        }
        if (!requireCount) {
            if (resource.containsKey("count")) throw LuaException("Filter resources must not include a count")
            return GenericStack(key, 0)
        }
        val count = (resource["count"] as? Number)?.toDouble() ?: throw LuaException("Resource count must be a positive integer")
        if (!count.isFinite() || count <= 0 || count % 1.0 != 0.0) throw LuaException("Resource count must be a positive integer")
        if (count >= Long.MAX_VALUE.toDouble()) throw LuaException("Resource count is too large")
        val amount = try {
            if (key is AEFluidKey) Math.multiplyExact(count.toLong(), PlatformToolkit.get().fluidCompactDivider.toLong()) else count.toLong()
        } catch (_: ArithmeticException) {
            throw LuaException("Resource count is too large")
        }
        return GenericStack(key, amount)
    }

    fun publicAmount(key: AEKey, amount: Long): Long = if (key is AEFluidKey) {
        amount / PlatformToolkit.get().fluidCompactDivider.toLong()
    } else {
        amount
    }

    fun keyCounterToLua(counter: KeyCounter, predicate: Predicate<AEKey> = ALWAYS, displayType: Boolean = false): List<Map<String, Any>> = counter
        .mapNotNull { entry ->
            val aeKey = entry.key
            when {
                !predicate.test(aeKey) -> null
                aeKey is AEItemKey -> {
                    val data = LuaRepresentation.forItemStack(aeKey.toStack(entry.longValue.toInt()))
                    data.remove("maxStackSize")
                    if (displayType) {
                        data["type"] = "item"
                    }
                    data
                }
                aeKey is AEFluidKey -> {
                    val data = mutableMapOf(
                        "name" to PlatformRegistries.FLUIDS.getKey(aeKey.fluid).toString(),
                        "amount" to entry.longValue / PlatformToolkit.get().fluidCompactDivider,
                    )
                    if (displayType) {
                        data["type"] = "fluid"
                    }
                    data
                }
                else -> null
            }
        }

    fun buildKey(mode: String, id_key: String): AEKey = when (mode) {
        "fluid" -> {
            val fluid = PlatformRegistries.FLUIDS.get(ResourceLocation.parse(id_key))
            AEFluidKey.of(fluid)
        }
        "item" -> {
            val item = PlatformRegistries.ITEMS.get(ResourceLocation.parse(id_key))
            AEItemKey.of(item)
        }
        else -> {
            throw LuaException("first argument should be 'fluid' or 'item'")
        }
    }
}
