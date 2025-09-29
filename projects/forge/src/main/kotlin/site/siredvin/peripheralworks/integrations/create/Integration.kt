package site.siredvin.peripheralworks.integrations.create

import com.simibubi.create.AllItems
import com.simibubi.create.content.contraptions.AbstractContraptionEntity
import com.simibubi.create.content.contraptions.piston.LinearActuatorBlockEntity
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import site.siredvin.broccolium.modules.storage.fluid.AgnosticFluidStorageLookup
import site.siredvin.broccolium.modules.storage.fluid.ForgeAgnosticFluidStorage
import site.siredvin.broccolium.modules.storage.fluid.api.AgnosticFluidStorageEntityExtractor
import site.siredvin.broccolium.modules.storage.item.AgnosticItemHandlerWrapper
import site.siredvin.broccolium.modules.storage.item.AgnosticItemStorageLookup
import site.siredvin.broccolium.modules.storage.item.api.AgnosticItemStorageEntityExtractor
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.common.item.EntityCard
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import java.util.function.Function
import kotlin.collections.set

class Integration : Runnable {

    object FilteringBehaviourPluginProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = "create_filter"

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val blockEntity = level.getBlockEntity(pos)
            if (!Configuration.enableCreateIntegration || blockEntity == null) return null
            if (blockEntity is SmartBlockEntity) {
                val behavior = blockEntity.getBehaviour(FilteringBehaviour.TYPE)
                if (behavior != null) {
                    return CreateFilterableBehaviorPeripheralPlugin(blockEntity, behavior)
                }
            }
            return null
        }
    }

    object ScrollingBehaviourPluginProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = "create_scroll"

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val blockEntity = level.getBlockEntity(pos)
            if (!Configuration.enableCreateIntegration || blockEntity == null) return null
            if (blockEntity is SmartBlockEntity) {
                val behaviour = blockEntity.getBehaviour(ScrollOptionBehaviour.TYPE)
                if (behaviour != null) {
                    return CreateScrollOptionPeripheralPlugin(blockEntity, behaviour)
                }
            }
            return null
        }
    }

    object CreatePluginProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = "create"

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val blockEntity = level.getBlockEntity(pos)
            if (!Configuration.enableCreateIntegration || blockEntity == null) return null

            if (blockEntity is SmartBlockEntity) {
                if (blockEntity is LinearActuatorBlockEntity) {
                    return CreateLinearActuatorPeripheralPlugin(blockEntity)
                } else {
                    val filterBehavior = blockEntity.getBehaviour(FilteringBehaviour.TYPE)
                    if (filterBehavior != null) {
                        return CreateFilterableBehaviorPeripheralPlugin(blockEntity, filterBehavior)
                    }
                }
            }
            return null
        }
    }

    override fun run() {
        ComputerCraftProxy.addProvider(CreatePluginProvider)
        ComputerCraftProxy.addProvider(FilteringBehaviourPluginProvider)
        ComputerCraftProxy.addProvider(ScrollingBehaviourPluginProvider)
        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)
        AgnosticItemStorageLookup.addItemStorageExtractor(
            AgnosticItemStorageEntityExtractor { level, entity ->
                if (entity is AbstractContraptionEntity) {
                    return@AgnosticItemStorageEntityExtractor AgnosticItemHandlerWrapper(entity.contraption.storage.allItems)
                }
                return@AgnosticItemStorageEntityExtractor null
            },
        )
        AgnosticFluidStorageLookup.addFluidStorageExtractor(
            AgnosticFluidStorageEntityExtractor { level, entity ->
                if (entity is AbstractContraptionEntity) {
                    return@AgnosticFluidStorageEntityExtractor ForgeAgnosticFluidStorage(entity.contraption.storage.fluids)
                }
                return@AgnosticFluidStorageEntityExtractor null
            },
        )
        EntityCard.EXTRA_SEARCHES[AllItems.WRENCH.get()] = Function<Player, Entity?> {
            val box = it.boundingBox.inflate(2.0)
            it.level().getEntitiesOfClass(AbstractContraptionEntity::class.java, box).firstOrNull()
        }
    }
}
