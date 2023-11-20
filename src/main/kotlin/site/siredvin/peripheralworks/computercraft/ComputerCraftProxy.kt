package site.siredvin.peripheralworks.computercraft

import dan200.computercraft.api.peripheral.IPeripheral
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Registry
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.common.ExtractorProxy
import site.siredvin.peripheralium.computercraft.peripheral.PluggablePeripheral
import site.siredvin.peripheralium.extra.plugins.AbstractFluidStoragePlugin
import site.siredvin.peripheralium.extra.plugins.FluidStoragePlugin
import site.siredvin.peripheralium.extra.plugins.InventoryPlugin
import site.siredvin.peripheralium.extra.plugins.ItemStoragePlugin
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.plugins.generic.DeferredFluidStoragePlugin
import site.siredvin.peripheralworks.computercraft.plugins.specific.SpecificPluginProvider
import site.siredvin.peripheralworks.tags.BlockTags

object ComputerCraftProxy {
    private val PLUGIN_PROVIDERS: MutableList<PeripheralPluginProvider> = mutableListOf()

    fun addProvider(provider: PeripheralPluginProvider) {
        PLUGIN_PROVIDERS.add(provider)
        PLUGIN_PROVIDERS.sort()
    }

    class FluidStorageProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = AbstractFluidStoragePlugin.PLUGIN_TYPE

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!PeripheralWorksConfig.enableGenericFluidStorage) {
                return null
            }
            val fluidStorage = FluidStorage.SIDED.find(level, pos, side) ?: return null
            val blockState = level.getBlockState(pos)
            if (blockState.`is`(BlockTags.DEFERRED_FLUID_STORAGE)) {
                return DeferredFluidStoragePlugin(level, pos, side)
            }
            return FluidStoragePlugin(level, fluidStorage)
        }
    }

    class InventoryProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = InventoryPlugin.PLUGIN_TYPE
        override val conflictWith: Set<String>
            get() = setOf(ItemStoragePlugin.PLUGIN_TYPE)

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!PeripheralWorksConfig.enableGenericInventory) {
                return null
            }
            val blockEntity = level.getBlockEntity(pos) ?: return null
            val itemStorage = ExtractorProxy.extractCCItemStorage(level, blockEntity) ?: return null
            if (itemStorage.size() == 0) {
                return null
            }
            return InventoryPlugin(level, itemStorage)
        }
    }

    class ItemStorageProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = ItemStoragePlugin.PLUGIN_TYPE
        override val priority: Int
            get() = 200
        override val conflictWith: Set<String>
            get() = setOf(InventoryPlugin.PLUGIN_TYPE)

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!PeripheralWorksConfig.enableGenericItemStorage) {
                return null
            }
            val itemStorage = ItemStorage.SIDED.find(level, pos, side) ?: return null
            Transaction.openOuter().use {
                var counting = 0
                itemStorage.iterator().forEach { _ ->
                    counting++
                }
                if (counting == 0) {
                    return null
                }
            }
            return ItemStoragePlugin(level, itemStorage)
        }
    }

    init {
        addProvider(FluidStorageProvider())
        addProvider(InventoryProvider())
        addProvider(ItemStorageProvider())
        addProvider(SpecificPluginProvider())
    }

    fun peripheralProvider(level: Level, pos: BlockPos, side: Direction): IPeripheral? {
        val entity = level.getBlockEntity(pos)
        val state = level.getBlockState(pos)
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

        if (plugins.isEmpty()) {
            return null
        }

        val peripheral = PluggablePeripheral(Registry.BLOCK.getKey(state.block).toString(), entity ?: pos)
        plugins.values.forEach { peripheral.addPlugin(it) }
        return peripheral
    }
}
