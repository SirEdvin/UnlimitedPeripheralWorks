package site.siredvin.peripheralworks.common

import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import java.util.function.Consumer

object RegistrationQueue {
    private val TURTLE_UPGRADE_SERIALIZER_QUEUE: MutableList<Consumer<Registry<TurtleUpgradeSerialiser<*>>>> = mutableListOf()
    private val POCKET_UPGRADE_SERIALIZER_QUEUE: MutableList<Consumer<Registry<PocketUpgradeSerialiser<*>>>> = mutableListOf()

    fun scheduleTurtleUpgrade(consumer: Consumer<Registry<TurtleUpgradeSerialiser<*>>>) {
        TURTLE_UPGRADE_SERIALIZER_QUEUE.add(consumer)
    }

    fun schedulePocketUpgrade(consumer: Consumer<Registry<PocketUpgradeSerialiser<*>>>) {
        POCKET_UPGRADE_SERIALIZER_QUEUE.add(consumer)
    }

    fun onNewRegistry(rawId: Int, id: ResourceLocation, registry: Registry<*>) {
        if (id.equals(TurtleUpgradeSerialiser.REGISTRY_ID.location())) {
            TURTLE_UPGRADE_SERIALIZER_QUEUE.forEach { it.accept(registry as Registry<TurtleUpgradeSerialiser<*>>) }
        } else if (id.equals(PocketUpgradeSerialiser.REGISTRY_ID.location())) {
            POCKET_UPGRADE_SERIALIZER_QUEUE.forEach { it.accept(registry as Registry<PocketUpgradeSerialiser<*>>) }
        }
    }
}
