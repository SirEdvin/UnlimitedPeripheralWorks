package site.siredvin.peripheralworks.computercraft.plugins.generic

import dan200.computercraft.shared.util.ItemStorage
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.common.ExtractorProxy
import site.siredvin.peripheralworks.api.PeripheralPluginProvider

class InventoryPlugin(override val level: Level, override val itemStorage: ItemStorage): AbstractInventoryPlugin() {

    /*Kotlin rework from https://github.com/cc-tweaked/cc-restitched/blob/mc-1.18.x%2Fstable/src/main/java/dan200/computercraft/shared/peripheral/generic/methods/InventoryMethods.java */

    companion object {
        const val PLUGIN_TYPE = "inventory"
    }

    class Provider: PeripheralPluginProvider {
        override val pluginType: String
            get() = PLUGIN_TYPE
        override val conflictWith: Set<String>
            get() = setOf(ItemStoragePlugin.PLUGIN_TYPE)

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val blockEntity = level.getBlockEntity(pos) ?: return null
            val itemStorage = ExtractorProxy.extractCCItemStorage(level, blockEntity) ?: return null
            if (itemStorage.size() == 0)
                return null
            return InventoryPlugin(level, itemStorage)
        }
    }

    override val additionalType: String
        get() = PLUGIN_TYPE
}