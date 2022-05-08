package site.siredvin.peripheralworks.computercraft.plugins.generic

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.shared.peripheral.generic.data.ItemData
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.common.ExtractorProxy
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import java.util.*
import java.util.function.Predicate
import kotlin.collections.HashMap

class ItemStoragePlugin(private val level: Level, private val storage: Storage<ItemVariant>): IPeripheralPlugin {
    companion object {
        const val PLUGIN_TYPE = "item_storage"
    }

    class Provider: PeripheralPluginProvider {
        override val pluginType: String
            get() = PLUGIN_TYPE
        override val priority: Int
            get() = 200
        override val conflictWith: Set<String>
            get() = setOf(InventoryPlugin.PLUGIN_TYPE)

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val itemStorage = ItemStorage.SIDED.find(level, pos, side) ?: return null
            Transaction.openOuter().use {
                val size = itemStorage.iterable(it).count()
                if (size == 0)
                    return null
            }
            return ItemStoragePlugin(level, itemStorage)
        }
    }

    @LuaFunction(mainThread = true)
    fun items(): List<Map<String, *>> {
        val result: MutableList<Map<String, *>> = mutableListOf()
        val transaction = Transaction.openOuter()
        transaction.use {
            storage.iterator(transaction).forEach {
                if (!it.isResourceBlank)
                    result.add(ItemData.fill(HashMap(), it.resource.toStack()))
            }
        }
        return result
    }

    @LuaFunction(mainThread = true)
    fun pushItem(computer: IComputerAccess, toName: String, itemName: Optional<String>, limit: Optional<Long>): Long {
        val location: IPeripheral = computer.getAvailablePeripheral(toName)
            ?: throw LuaException("Target '$toName' does not exist")

        val toStorage = ExtractorProxy.extractItemStorage(level, location.target)
            ?: throw LuaException("Target '$toName' is not an fluid inventory")

        val predicate: Predicate<ItemVariant> = if (itemName.isEmpty) {
            Predicate { true }
        } else {
            val item = Registry.ITEM.get(ResourceLocation(itemName.get()))
            if (item == Items.AIR)
                throw LuaException("There is no item ${itemName.get()}")
            Predicate { it.isOf(item) }
        }
        return StorageUtil.move(storage, toStorage, predicate, limit.orElse(Long.MAX_VALUE), null)
    }

    @LuaFunction(mainThread = true)
    fun pullItem(computer: IComputerAccess, fromName: String, itemName: Optional<String>, limit: Optional<Long>): Long {
        val location: IPeripheral = computer.getAvailablePeripheral(fromName)
            ?: throw LuaException("Target '$fromName' does not exist")

        val fromStorage = ExtractorProxy.extractItemStorage(level, location.target)
            ?: throw LuaException("Target '$fromName' is not an fluid inventory")

        val predicate: Predicate<ItemVariant> = if (itemName.isEmpty) {
            Predicate { true }
        } else {
            val item = Registry.ITEM.get(ResourceLocation(itemName.get()))
            if (item == Items.AIR)
                throw LuaException("There is no item ${itemName.get()}")
            Predicate { it.isOf(item) }
        }
        return StorageUtil.move(fromStorage, storage, predicate, limit.orElse(Long.MAX_VALUE), null)
    }
}