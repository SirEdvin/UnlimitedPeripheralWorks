package site.siredvin.peripheralworks.integrations.emi

import com.google.gson.Gson
import dan200.computercraft.client.gui.AbstractComputerScreen
import dan200.computercraft.shared.ModRegistry
import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.stack.FluidEmiStack
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.peripheralworks.networking.ClientNetworking
import site.siredvin.peripheralworks.networking.MapBasedEventMessage
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation
import site.siredvin.tweakium.modules.peripheral.representation.RepresentationMode
import site.siredvin.tweakium.modules.platform.ComputerPlatformToolkit
import kotlin.collections.set

object CommonEntrypoint {
    val GSON = Gson()

    fun mapStack(stack: EmiStack): MutableMap<String, Any> {
        if (stack.isEmpty) {
            return mutableMapOf(
                "type" to "empty",
            )
        }
        if (!stack.itemStack.isEmpty) {
            val base = LuaRepresentation.forItemStack(stack.itemStack, RepresentationMode.BASE)
            base["type"] = "item"
            base["chance"] = stack.chance
            return base
        }
        val base = mutableMapOf<String, Any>()
        base["name"] = stack.id.toString()
        base["amount"] = stack.amount
        base["displayName"] = stack.name.string
        base["chance"] = stack.chance
        if (stack.nbt != null) {
            val nbtHash = ComputerPlatformToolkit.get().nbtHash(stack.nbt)
            if (nbtHash != null) {
                base["nbt"] = nbtHash
            }
        }
        if (stack is FluidEmiStack) {
            base["type"] = "fluid"
            base["amount"] = (base["amount"] as Number).toDouble() / PlatformToolkit.get().fluidCompactDivider
        }
        return base
    }

    fun mapIngredient(ingredient: EmiIngredient): MutableMap<String, Any> {
        if (ingredient.isEmpty) {
            return mutableMapOf(
                "type" to "empty",
            )
        }
        val ingredients = ingredient.emiStacks.map(::mapStack)
        if (ingredients.isEmpty()) {
            return mutableMapOf(
                "type" to "empty",
            )
        }
        if (ingredients.size == 1) {
            return ingredients[0]
        }
        return mutableMapOf("candidates" to ingredients)
    }

    fun register(registry: EmiRegistry) {
        registry.addGenericDragDropHandler { screen, stack, x, y ->
            if (screen is AbstractComputerScreen<*>) {
                ClientNetworking.sendToServer(MapBasedEventMessage(screen.menu, "emi_ingredient_paste", mapIngredient(stack)))
                return@addGenericDragDropHandler true
            }
            return@addGenericDragDropHandler false
        }
        registry.addRecipeHandler(ModRegistry.Menus.COMPUTER.get(), ComputerRecipeHandler())
    }
}
