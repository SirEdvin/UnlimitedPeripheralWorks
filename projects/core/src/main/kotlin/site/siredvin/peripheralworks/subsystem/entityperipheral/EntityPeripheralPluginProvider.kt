package site.siredvin.peripheralworks.subsystem.entityperipheral

import net.minecraft.world.entity.Entity
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin

interface EntityPeripheralPluginProvider : Comparable<EntityPeripheralPluginProvider> {
    val pluginType: String

    /**
     * Determinate order of plugin loading, pretty important if you have incompatible or same plugin and want to load it before
     * Lower priority wins
     */
    val priority: Int
        get() = 100
    val conflictWith: Set<String>
        get() = emptySet()

    fun provide(entity: Entity): IPeripheralPlugin?

    override operator fun compareTo(other: EntityPeripheralPluginProvider): Int {
        return priority - other.priority
    }
}
