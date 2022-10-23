package site.siredvin.peripheralworks.computercraft.plugins.generic

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.world.level.material.Fluids
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.common.ExtractorProxy
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import java.util.Optional
import java.util.function.Predicate
import kotlin.math.min

class FluidStoragePlugin(private val level: Level, private val storage: Storage<FluidVariant>): IPeripheralPlugin {

    companion object {
        const val FORGE_COMPACT_DEVIDER = 81.0
        const val PLUGIN_TYPE = "fluid_storage"
    }

    class Provider: PeripheralPluginProvider {
        override val pluginType: String
            get() = PLUGIN_TYPE

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!PeripheralWorksConfig.enableGenericFluidStorage)
                return null
            val fluidStorage = FluidStorage.SIDED.find(level, pos, side) ?: return null
            return FluidStoragePlugin(level, fluidStorage)
        }

    }

    override val additionalType: String
        get() = PLUGIN_TYPE

    @LuaFunction(mainThread = true)
    fun tanks(): List<Map<String, *>> {
        val data: MutableList<Map<String, *>> = mutableListOf()
        storage.iterator().forEach {
            data.add(hashMapOf(
                "name" to Registry.FLUID.getKey(it.resource.fluid).toString(),
                "amount" to it.amount / FORGE_COMPACT_DEVIDER,
                "capacity" to it.capacity / FORGE_COMPACT_DEVIDER
            ))
        }
        return data
    }

    @LuaFunction(mainThread = true)
    fun pushFluid(computer: IComputerAccess, toName: String, limit: Optional<Long>, fluidName: Optional<String>): Double {
        val location: IPeripheral = computer.getAvailablePeripheral(toName)
            ?: throw LuaException("Target '$toName' does not exist")

        val toStorage = ExtractorProxy.extractFluidStorage(level, location.target)
            ?: throw LuaException("Target '$toName' is not an fluid inventory")

        val predicate: Predicate<FluidVariant> = if (fluidName.isEmpty) {
            Predicate { true }
        } else {
            val fluid = Registry.FLUID.get(ResourceLocation(fluidName.get()))
            if (fluid.isSame(Fluids.EMPTY))
                throw LuaException("There is no fluid ${fluidName.get()}")
            Predicate { it.fluid.isSame(fluid) }
        }
        val realLimit = min(PeripheralWorksConfig.fluidStorageTransferLimit, limit.map { it * FORGE_COMPACT_DEVIDER.toLong() }.orElse(Long.MAX_VALUE))
        return StorageUtil.move(storage, toStorage, predicate, realLimit, null) / FORGE_COMPACT_DEVIDER
    }

    @LuaFunction(mainThread = true)
    fun pullFluid(computer: IComputerAccess, fromName: String, limit: Optional<Long>, fluidName: Optional<String>): Double {
        val location: IPeripheral = computer.getAvailablePeripheral(fromName)
            ?: throw LuaException("Target '$fromName' does not exist")

        val fromStorage = ExtractorProxy.extractFluidStorage(level, location.target)
            ?: throw LuaException("Target '$fromName' is not an fluid inventory")

        val predicate: Predicate<FluidVariant> = if (fluidName.isEmpty) {
            Predicate { true }
        } else {
            val fluid = Registry.FLUID.get(ResourceLocation(fluidName.get()))
            if (fluid.isSame(Fluids.EMPTY))
                throw LuaException("There is no fluid ${fluidName.get()}")
            Predicate { it.fluid.isSame(fluid) }
        }
        val realLimit = min(PeripheralWorksConfig.fluidStorageTransferLimit, limit.map { it * FORGE_COMPACT_DEVIDER.toLong() }.orElse(Long.MAX_VALUE))
        return StorageUtil.move(fromStorage, storage, predicate, realLimit, null) / FORGE_COMPACT_DEVIDER
    }
}