package site.siredvin.peripheralworks.subsystem.entityperipheral

import net.minecraft.world.entity.Entity
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin

object EntityPeripheralLookup {
    private val PLUGIN_PROVIDERS: MutableList<EntityPeripheralPluginProvider> = mutableListOf()

    fun addProvider(provider: EntityPeripheralPluginProvider) {
        PLUGIN_PROVIDERS.add(provider)
        PLUGIN_PROVIDERS.sort()
    }

    fun collectPlugins(entity: Entity): Map<String, IPeripheralPlugin> {
        if (entity.isRemoved) return emptyMap()

        val plugins: MutableMap<String, IPeripheralPlugin> = mutableMapOf()
        val deniedPluginTypes: MutableSet<String> = mutableSetOf()

        PLUGIN_PROVIDERS.forEach {
            if (!plugins.containsKey(it.pluginType) && !deniedPluginTypes.contains(it.pluginType) && !it.conflictWith.any { pluginType -> plugins.containsKey(pluginType) }) {
                val plugin = it.provide(entity)
                if (plugin != null) {
                    plugins[plugin.additionalType ?: it.pluginType] = plugin
                    deniedPluginTypes.addAll(it.conflictWith)
                }
            }
        }
        return plugins
    }
}
