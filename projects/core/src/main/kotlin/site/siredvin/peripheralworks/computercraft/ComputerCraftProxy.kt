package site.siredvin.peripheralworks.computercraft

import dan200.computercraft.api.peripheral.IPeripheral
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.broccolium.modules.platform.PlatformRegistries
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.computercraft.peripherals.PluggablePeripheral
import site.siredvin.peripheralworks.tags.BlockTags
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralBlockEntity
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import site.siredvin.tweakium.modules.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.owner.BlockPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.owner.RawBlockEntityPeripheralOwner
import java.util.function.Supplier

object ComputerCraftProxy {
    private val PLUGIN_PROVIDERS: MutableList<PeripheralPluginProvider> = mutableListOf()

    fun addProvider(provider: PeripheralPluginProvider) {
        PLUGIN_PROVIDERS.add(provider)
        PLUGIN_PROVIDERS.sort()
    }

    fun collectPlugins(level: Level, pos: BlockPos, side: Direction): Map<String, IPeripheralPlugin> {
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
        return plugins
    }

    private fun selectPeripheralOwner(entity: BlockEntity?, pos: BlockPos, level: Level): IPeripheralOwner {
        if (entity != null) {
            if (entity is IPeripheralBlockEntity) {
                return BlockEntityPeripheralOwner(entity)
            }
            return RawBlockEntityPeripheralOwner(entity)
        }
        return BlockPeripheralOwner(pos, level)
    }

    fun lazyPeripheralProvider(level: Level, pos: BlockPos, side: Direction): Supplier<IPeripheral>? {
        val state = level.getBlockState(pos)
        if (state.`is`(BlockTags.IGNORE)) {
            return null
        }
        val plugins = collectPlugins(level, pos, side)
        if (plugins.isEmpty()) {
            return null
        }
        return Supplier {
            val state = level.getBlockState(pos)
            val entity = level.getBlockEntity(pos)
            val peripheral = PluggablePeripheral(PlatformRegistries.BLOCKS.getKey(state.block).toString(), selectPeripheralOwner(entity, pos, level), side)
            plugins.values.forEach { peripheral.addPlugin(it) }
            return@Supplier peripheral
        }
    }

    fun peripheralProvider(level: Level, pos: BlockPos, state: BlockState, entity: BlockEntity?, side: Direction): IPeripheral? {
        if (state.`is`(BlockTags.IGNORE)) return null
        val plugins = collectPlugins(level, pos, side)
        if (plugins.isEmpty()) {
            return null
        }

        val peripheral = PluggablePeripheral(PlatformRegistries.BLOCKS.getKey(state.block).toString(), selectPeripheralOwner(entity, pos, level), side)
        plugins.values.forEach { peripheral.addPlugin(it) }
        return peripheral
    }
}
