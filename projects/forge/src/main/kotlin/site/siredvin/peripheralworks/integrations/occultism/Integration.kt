package site.siredvin.peripheralworks.integrations.occultism

import com.klikli_dev.occultism.api.common.blockentity.IStorageController
import com.klikli_dev.occultism.api.common.blockentity.IStorageControllerProxy
import com.klikli_dev.occultism.common.blockentity.GoldenSacrificialBowlBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.extra.plugins.PeripheralPluginUtils
import site.siredvin.peripheralium.storages.item.ItemStorageExtractor
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy

class Integration : Runnable {

    object OccultismStorageProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = "occultism_storage"

        override val priority: Int
            get() = 50

        override val conflictWith: Set<String>
            get() = setOf(PeripheralPluginUtils.Type.INVENTORY, PeripheralPluginUtils.Type.ITEM_STORAGE)
        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!Configuration.enableOccultismStorage) {
                return null
            }
            val blockEntity = level.getBlockEntity(pos)
            if (blockEntity is IStorageController) {
                return OccultismItemStoragePlugin(blockEntity, level)
            }
            if (blockEntity is IStorageControllerProxy) {
                return OccultismItemStoragePlugin(blockEntity.linkedStorageController, level)
            }
            return null
        }
    }

    object SpecificOccultismPluginProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = "occultism"

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val entity = level.getBlockEntity(pos) ?: return null
            return when (entity::class.java) {
                GoldenSacrificialBowlBlockEntity::class.java -> if (Configuration.enableOccultismGoldenBowl) {
                    GoldenSacrificialBowlPlugin(
                        entity as GoldenSacrificialBowlBlockEntity,
                    )
                } else {
                    null
                }

                else -> null
            }
        }
    }

    override fun run() {
        ComputerCraftProxy.addProvider(OccultismStorageProvider)
        ComputerCraftProxy.addProvider(SpecificOccultismPluginProvider)
        ItemStorageExtractor.addStorageExtractor(
            ItemStorageExtractor.StorageExtractor { _, _, blockEntity ->
                if (blockEntity == null || blockEntity.isRemoved) {
                    return@StorageExtractor null
                }
                if (blockEntity is IStorageController) {
                    return@StorageExtractor OccultismItemStorage(blockEntity)
                }
                if (blockEntity is IStorageControllerProxy) {
                    return@StorageExtractor OccultismItemStorage(blockEntity.linkedStorageController)
                }
                return@StorageExtractor null
            },
        )
        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)
    }
}
