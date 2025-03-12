package site.siredvin.peripheralworks.integrations.create

import com.simibubi.create.content.contraptions.piston.LinearActuatorBlockEntity
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

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
                if (blockEntity is BlazeBurnerBlockEntity) {
                    return CreateBlazeBurnerPeripheralPlugin(blockEntity)
                } else if (blockEntity is LinearActuatorBlockEntity) {
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
    }
}
