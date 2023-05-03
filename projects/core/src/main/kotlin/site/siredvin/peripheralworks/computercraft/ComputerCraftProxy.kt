package site.siredvin.peripheralworks.computercraft

import dan200.computercraft.api.peripheral.IPeripheral
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.computercraft.peripheral.PluggablePeripheral
import site.siredvin.peripheralium.xplat.XplatRegistries
import site.siredvin.peripheralworks.api.PeripheralPluginProvider

object ComputerCraftProxy {
    private val PLUGIN_PROVIDERS: MutableList<PeripheralPluginProvider> = mutableListOf()

    fun addProvider(provider: PeripheralPluginProvider) {
        PLUGIN_PROVIDERS.add(provider)
        PLUGIN_PROVIDERS.sort()
    }

    fun peripheralProvider(level: Level, pos: BlockPos, state: BlockState, entity: BlockEntity?, side: Direction): IPeripheral? {
        val plugins: MutableMap<String, IPeripheralPlugin> = mutableMapOf()
        val deniedPluginTypes: MutableSet<String> = mutableSetOf()

        PLUGIN_PROVIDERS.forEach {
            if (!plugins.containsKey(it.pluginType) && !deniedPluginTypes.contains(it.pluginType) && !it.conflictWith.any { pluginType -> plugins.containsKey(pluginType) }) {
                val plugin = it.provide(level, pos, side)
                if (plugin != null) {
                    plugins[plugin.additionalType ?: it.pluginType] = plugin
                    deniedPluginTypes.addAll(it.conflictWith)
                }
            }
        }

        if (plugins.isEmpty())
            return null

        val peripheral = PluggablePeripheral(XplatRegistries.BLOCKS.getKey(state.block).toString(), entity ?: pos)
        plugins.values.forEach { peripheral.addPlugin(it) }
        return peripheral
    }
}