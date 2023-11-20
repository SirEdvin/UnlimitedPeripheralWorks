package site.siredvin.peripheralworks.api

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin

interface PeripheralPluginProvider : Comparable<PeripheralPluginProvider> {
    val pluginType: String

    /**
     * Determinate order of plugin loading, pretty important if you have incompatible or same plugin and want to load it before
     * Lower priority wins
     */
    val priority: Int
        get() = 100
    val conflictWith: Set<String>
        get() = emptySet()

    fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin?

    override operator fun compareTo(other: PeripheralPluginProvider): Int {
        return priority - other.priority
    }
}
