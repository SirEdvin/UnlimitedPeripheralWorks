package site.siredvin.peripheralworks.computercraft

import dan200.computercraft.api.peripheral.IPeripheral
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Registry
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.computercraft.peripheral.PluggablePeripheral
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.computercraft.plugins.generic.FluidStoragePlugin
import site.siredvin.peripheralworks.computercraft.plugins.generic.InventoryPlugin
import site.siredvin.peripheralworks.computercraft.plugins.generic.ItemStoragePlugin
import site.siredvin.peripheralworks.computercraft.plugins.specific.SpecificPluginProvider

object ComputerCraftProxy {
    private val PLUGIN_PROVIDERS: MutableList<PeripheralPluginProvider> = mutableListOf()

    fun addProvider(provider: PeripheralPluginProvider) {
        PLUGIN_PROVIDERS.add(provider)
        PLUGIN_PROVIDERS.sort()
    }

    init {
        addProvider(FluidStoragePlugin.Provider())
        addProvider(InventoryPlugin.Provider())
        addProvider(ItemStoragePlugin.Provider())
        addProvider(SpecificPluginProvider())
    }

    fun peripheralProvider(level: Level, pos: BlockPos, side: Direction): IPeripheral? {
        val entity = level.getBlockEntity(pos)
        val state = level.getBlockState(pos)
        val plugins: MutableMap<String, IPeripheralPlugin> = mutableMapOf()

        PLUGIN_PROVIDERS.forEach {
            if (!plugins.containsKey(it.pluginType) && !it.conflictWith.any { pluginType -> plugins.containsKey(pluginType) }) {
                val plugin = it.provide(level, pos, side)
                if (plugin != null) {
                    plugins[plugin.additionalType ?: it.pluginType] = plugin
                }
            }
        }

        if (plugins.isEmpty())
            return null

        val peripheral = PluggablePeripheral(Registry.BLOCK.getKey(state.block).toString(), entity ?: pos)
        plugins.values.forEach { peripheral.addPlugin(it) }
        return peripheral
    }
}