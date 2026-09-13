package site.siredvin.peripheralworks.computercraft

import net.minecraft.ResourceLocationException
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Predicate

/** Registered only by available integrations; no optional model classes enter the shared loader. */
object NeuralModelNameGuard {
    private val invalidModels = CopyOnWriteArrayList<Predicate<ItemStack>>()

    fun register(predicate: Predicate<ItemStack>) {
        invalidModels.add(predicate)
    }

    @JvmStatic
    fun nativeName(stack: ItemStack): Component {
        if (!invalidModels.any { it.test(stack) }) return stack.item.getName(stack)
        return try {
            stack.item.getName(stack)
        } catch (_: ResourceLocationException) {
            Component.translatable(stack.item.descriptionId)
        } catch (_: IndexOutOfBoundsException) {
            Component.translatable(stack.item.descriptionId)
        }
    }
}
