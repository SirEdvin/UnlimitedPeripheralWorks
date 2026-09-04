package site.siredvin.peripheralworks.integrations.occultism

import com.klikli_dev.occultism.api.common.blockentity.IStorageController
import com.klikli_dev.occultism.api.common.blockentity.IStorageControllerProxy
import com.klikli_dev.occultism.common.blockentity.GoldenSacrificialBowlBlockEntity
import com.klikli_dev.occultism.common.entity.spirit.SpiritEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.broccolium.modules.storage.item.AgnosticItemHandlerWrapper
import site.siredvin.broccolium.modules.storage.item.AgnosticItemStorageLookup
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.integration.OccultismConfiguration
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.computercraft.peripherals.EntityLinkPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import site.siredvin.tweakium.modules.plugins.PeripheralPluginUtils
import java.util.function.BiConsumer

class Integration : Runnable {

    object OccultismStorageProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = "occultism_storage"

        override val priority: Int
            get() = 50

        override val conflictWith: Set<String>
            get() = setOf(PeripheralPluginUtils.Type.INVENTORY, PeripheralPluginUtils.Type.ITEM_STORAGE)
        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!OccultismConfiguration.enableOccultismStorage) {
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
                GoldenSacrificialBowlBlockEntity::class.java -> if (OccultismConfiguration.enableOccultismGoldenBowl) {
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
        AgnosticItemStorageLookup.addBlockLookup { _, _, blockEntity, direction ->
            if (blockEntity == null || blockEntity.isRemoved) {
                return@addBlockLookup null
            }
            if (blockEntity is IStorageController) {
                return@addBlockLookup OccultismItemStorage(blockEntity)
            }
            if (blockEntity is IStorageControllerProxy) {
                return@addBlockLookup OccultismItemStorage(blockEntity.linkedStorageController)
            }
            return@addBlockLookup null
        }
        AgnosticItemStorageLookup.addEntityLookup { _, entity, direction ->
            if (entity is SpiritEntity) {
                return@addEntityLookup AgnosticItemHandlerWrapper(entity.inventory)
            }
            return@addEntityLookup null
        }
        EntityLinkPeripheral.ENRICHERS.add(
            BiConsumer { entity, data ->
                if (entity is SpiritEntity) {
                    data["age"] = entity.spiritAge
                    data["maxAge"] = entity.spiritMaxAge
                }
            },
        )
    }
}
