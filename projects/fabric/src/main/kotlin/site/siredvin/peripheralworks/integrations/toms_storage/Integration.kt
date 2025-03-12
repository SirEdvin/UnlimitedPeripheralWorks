@file:Suppress("ktlint:standard:package-name")

package site.siredvin.peripheralworks.integrations.toms_storage

import com.tom.storagemod.tile.InventoryConnectorBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import site.siredvin.tweakium.modules.plugins.PeripheralPluginUtils

class Integration : Runnable {

    object TomsItemStoragePluginProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = "toms_storage"

        override val conflictWith: Set<String>
            get() = setOf(PeripheralPluginUtils.Type.ITEM_STORAGE, PeripheralPluginUtils.Type.INVENTORY)

        override val priority: Int
            get() = 50

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!Configuration.enableTomsStorage) {
                return null
            }
            val entity = level.getBlockEntity(pos) as? InventoryConnectorBlockEntity ?: return null
            return TomsItemStoragePlugin(entity)
        }
    }

    override fun run() {
        ComputerCraftProxy.addProvider(TomsItemStoragePluginProvider)
        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)
    }
}
