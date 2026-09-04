package site.siredvin.peripheralworks.integrations.integrateddynamics

import dan200.computercraft.api.ComputerCraftAPI
import dan200.computercraft.shared.computer.blocks.AbstractComputerBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.world.level.Level
import org.cyclops.cyclopscore.datastructure.DimPos
import org.cyclops.integrateddynamics.IntegratedDynamics
import org.cyclops.integrateddynamics.api.part.aspect.AspectUpdateType
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeNbt
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes
import org.cyclops.integrateddynamics.core.part.PartTypes
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry
import org.cyclops.integrateddynamics.core.part.aspect.build.AspectBuilder
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBuilders
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.integration.IntegratedDynamicsConfiguration
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.data.ModEnLanguageProvider
import site.siredvin.peripheralworks.data.ModLanguageProvider
import site.siredvin.peripheralworks.data.ModUaLanguageProvider
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import site.siredvin.tweakium.modules.plugins.PeripheralPluginUtils

class Integration : Runnable {

    companion object {
        val EMPTY_TAG = CompoundTag()
    }

    object VariableStoreProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = "variable_store"

        override val priority: Int
            get() = 50

        override val conflictWith: Set<String>
            get() = setOf(PeripheralPluginUtils.Type.INVENTORY, PeripheralPluginUtils.Type.ITEM_STORAGE)
        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!IntegratedDynamicsConfiguration.enableVariableStore) {
                return null
            }
            val blockEntity = level.getBlockEntity(pos)
            if (blockEntity is BlockEntityVariablestore) {
                return VariableStorePlugin(blockEntity)
            }
            return null
        }
    }

    private fun collectValue(it: DimPos): CompoundTag {
        val level = it.getLevel(false) ?: return EMPTY_TAG
        val entity = level.getBlockEntity(it.blockPos)
        val computer = entity as? AbstractComputerBlockEntity ?: return EMPTY_TAG
        return IntegratedDynamicProxy.outputData.getOrDefault(computer.computerID, EMPTY_TAG)
    }
    private fun setValue(it: DimPos, tag: Tag) {
        val level = it.getLevel(false) ?: return
        val entity = level.getBlockEntity(it.blockPos)
        val computer = entity as? AbstractComputerBlockEntity ?: return
        IntegratedDynamicProxy.inputData[computer.computerID] = tag
    }

    override fun run() {
        ComputerCraftProxy.addProvider(VariableStoreProvider)
        val ccOutput = AspectBuilder.forReadType(ValueTypes.NBT).byMod(IntegratedDynamics._instance)
            .handle(AspectReadBuilders.Block.PROP_GET, "cc_output")
            .handle {
                ValueTypeNbt.ValueNbt.of(collectValue(it))
            }.withUpdateType(AspectUpdateType.NETWORK_TICK).buildRead()
        val ccInput = AspectBuilder.forWriteType(ValueTypes.NBT).byMod(IntegratedDynamics._instance)
            .handle({
                if (it.right.value.rawValue.isPresent) {
                    setValue(it.left.target.pos, it.right.value.rawValue.get())
                }
            }, "cc_input").buildWrite()
        if (IntegratedDynamicsConfiguration.enableComputerAspect) {
            AspectRegistry.getInstance().register(PartTypes.MACHINE_READER, ccOutput)
            AspectRegistry.getInstance().register(PartTypes.MACHINE_WRITER, ccInput)
        }
        ComputerCraftAPI.registerAPIFactory {
            IntegratedDynamicsAPI(it.id)
        }

        ModLanguageProvider.addExpectedKey("aspect.integrateddynamics.read.nbt.cc_output")
        ModLanguageProvider.addExpectedKey("aspect.integrateddynamics.write.nbt.cc_input")
        ModEnLanguageProvider.addHook {
            it.add("aspect.integrateddynamics.read.nbt.cc_output", "Output information from computer")
            it.add("aspect.integrateddynamics.write.nbt.cc_input", "Input information to computer")
        }
        ModUaLanguageProvider.addHook {
            it.add("aspect.integrateddynamics.read.nbt.cc_output", "Інформація від комп'ютера")
            it.add("aspect.integrateddynamics.write.nbt.cc_input", "Інформація для комп'ютера")
        }
    }
}
