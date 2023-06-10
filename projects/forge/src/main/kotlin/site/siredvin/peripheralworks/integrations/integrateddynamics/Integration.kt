package site.siredvin.peripheralworks.integrations.integrateddynamics

import dan200.computercraft.core.apis.handles.EncodedReadableHandle
import dan200.computercraft.core.filesystem.FileSystemException
import dan200.computercraft.shared.computer.blocks.AbstractComputerBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
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
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.extra.plugins.PeripheralPluginUtils
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.data.ModEnLanguageProvider
import site.siredvin.peripheralworks.data.ModLanguageProvider

class Integration : Runnable {

    companion object {
        const val FILESYSTEM_PATH = "/integrateddynamics"
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
            if (!Configuration.enableVariableStore) {
                return null
            }
            val blockEntity = level.getBlockEntity(pos)
            if (blockEntity is BlockEntityVariablestore) {
                return VariableStorePlugin(blockEntity)
            }
            return null
        }
    }

    private fun isExceptionExpected(exception: IllegalStateException): Boolean {
        return exception.message == "FileSystem has not been created yet"
    }

    private fun collectFileValues(it: DimPos): CompoundTag {
        val level = it.getLevel(false) ?: return EMPTY_TAG
        val entity = level.getBlockEntity(it.blockPos)
        val computer = entity as? AbstractComputerBlockEntity ?: return EMPTY_TAG
        val serverComputer = computer.serverComputer ?: return EMPTY_TAG
        try {
            val fileSystem = serverComputer.computer.environment.fileSystem
            if (!fileSystem.exists(FILESYSTEM_PATH) || !fileSystem.isDir(FILESYSTEM_PATH)) {
                return CompoundTag()
            }
            val buffer = CompoundTag()
            fileSystem.list(FILESYSTEM_PATH).forEach { filePath ->
                val wrapper = fileSystem.openForRead("$FILESYSTEM_PATH/$filePath", EncodedReadableHandle::openUtf8)
                val handle = EncodedReadableHandle(wrapper.get(), wrapper)
                val stringBuilder = StringBuilder()
                handle.readAll()?.forEach { line -> stringBuilder.append(line) }
                buffer.putString(filePath, stringBuilder.toString())
                wrapper.close()
            }
            return buffer
        } catch (ignored: FileSystemException) {
            PeripheralWorksCore.LOGGER.warn("File system exception when trying to get ccAspect from ${it.blockPos}")
            PeripheralWorksCore.LOGGER.error(ignored)
            return EMPTY_TAG
        } catch (ignored: IllegalStateException) {
            if (!isExceptionExpected(ignored)) {
                PeripheralWorksCore.LOGGER.warn("Illegal state exception when trying to get ccAspect from ${it.blockPos}")
                PeripheralWorksCore.LOGGER.error(ignored)
            }
            return EMPTY_TAG
        }
    }

    override fun run() {
        ComputerCraftProxy.addProvider(VariableStoreProvider)
        val ccAspect = AspectBuilder.forReadType(ValueTypes.LIST).byMod(IntegratedDynamics._instance)
            .handle(AspectReadBuilders.Block.PROP_GET, "cc_folder")
            .handle {
                ValueTypeNbt.ValueNbt.of(collectFileValues(it))
            }.withUpdateType(AspectUpdateType.NETWORK_TICK).buildRead()
        AspectRegistry.getInstance().register(PartTypes.MACHINE_READER, ccAspect)
        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)

        ModLanguageProvider.addExpectedKey("aspect.integrateddynamics.read.list.cc_folder")
        ModEnLanguageProvider.addHook {
            it.add("aspect.integrateddynamics.read.list.cc_folder", "Directory inside computer")
        }
    }
}
