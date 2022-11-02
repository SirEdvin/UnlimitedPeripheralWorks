package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.stacks.*
import dan200.computercraft.api.lua.LuaException
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralium.extra.plugins.FluidStoragePlugin
import site.siredvin.peripheralium.util.representation.LuaRepresentation
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.function.Predicate

object AE2Helper {

    private val ALWAYS: Predicate<AEKey> = Predicate{ true }
    val CRAFTING_JOB_EXECUTOR: ExecutorService = Executors.newCachedThreadPool(ThreadFactory {
        val crafting = Thread(it, "AE Crafting Calculator for ComputerCraft")
        crafting.isDaemon = true
        return@ThreadFactory crafting
    })

    init {
    }

    fun genericStackToMap(stack: GenericStack): MutableMap<String, Any> {
        if (stack.what is AEItemKey) {
            val base = LuaRepresentation.forItemStack((stack.what as AEItemKey).toStack(stack.amount.toInt()))
            base["type"] = "item"
            return base
        }
        val base = mutableMapOf<String, Any>()
        base["type"] = "fluid"
        base["name"] = Registry.FLUID.getKey((stack.what as AEFluidKey).fluid).toString()
        base["count"] = stack.amount.toInt() / FluidStoragePlugin.FORGE_COMPACT_DEVIDER
        return base
    }

    fun keyCounterToLua(counter: KeyCounter, predicate: Predicate<AEKey> = ALWAYS, displayType: Boolean = false): MutableList<Map<String, Any>> {
        val items = mutableListOf<Map<String, Any>>()
        counter.forEach {
            val aeKey = it.key
            if (predicate.test(aeKey)) {
                if (aeKey is AEItemKey) {
                    val data = LuaRepresentation.forItemStack(aeKey.toStack(it.longValue.toInt()))
                    data.remove("maxStackSize")
                    if (displayType)
                        data["type"] = "item"
                    items.add(data)
                } else if (aeKey is AEFluidKey) {
                    val data = mutableMapOf(
                        "name" to Registry.FLUID.getKey(aeKey.fluid).toString(),
                        "amount" to it.longValue / FluidStoragePlugin.FORGE_COMPACT_DEVIDER,
                    )
                    if (displayType)
                        data["type"] = "fluid"
                }
            }
        }
        return items
    }

    fun buildKey(mode: String, id_key: String): AEKey {
        return when (mode) {
            "fluid" -> {
                val fluid = Registry.FLUID.get(ResourceLocation(id_key))
                AEFluidKey.of(fluid)
            }
            "item" -> {
                val item = Registry.ITEM.get(ResourceLocation(id_key))
                AEItemKey.of(item)
            }
            else -> {
                throw LuaException("first argument should be 'fluid' or 'item'")
            }
        }
    }
}