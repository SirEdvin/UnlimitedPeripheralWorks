package site.siredvin.peripheralworks.computercraft.plugins.generic

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.shared.peripheral.generic.data.ItemData
import dan200.computercraft.shared.util.ItemStorage

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.util.assertBetween
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.ExtractorProxy
import site.siredvin.peripheralworks.util.InventoryDealer
import java.util.*

class InventoryPlugin(private val itemStorage: ItemStorage): IPeripheralPlugin {

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
            val itemStorage = ExtractorProxy.extractCCItemStorage(blockEntity) ?: return null
            if (itemStorage.size() == 0)
                return null
            return InventoryPlugin(itemStorage)
        }
    }

    override val additionalType: String
        get() = PLUGIN_TYPE

    @LuaFunction(mainThread = true)
    fun size(): Int {
        return itemStorage.size()
    }

    @LuaFunction(mainThread = true)
    fun list(): Map<Int, Map<String, *>> {
        val result: MutableMap<Int, Map<String, *>> = hashMapOf()
        val size = itemStorage.size()
        for (i in 0 until size) {
            val stack = itemStorage.getStack(i)
            if (!stack.isEmpty) result[i + 1] =
                ItemData.fillBasic(HashMap(4), stack)
        }
        return result
    }

    @LuaFunction(mainThread = true)
    fun getItemDetail(slot: Int): Map<String, *>? {
        assertBetween(slot, 1, itemStorage.size(), "slot")
        val stack = itemStorage.getStack(slot - 1)
        return if (stack.isEmpty) null else ItemData.fill(HashMap(), stack)
    }

    @LuaFunction(mainThread = true)
    fun getItemLimit(slot: Int): Int {
        assertBetween(slot, 1, itemStorage.size(), "slot")
        return itemStorage.getStack(slot - 1).maxStackSize
    }

    @LuaFunction(mainThread = true)
    @Throws(LuaException::class)
    fun pushItems(computer: IComputerAccess, toName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int {

        // Find location to transfer to
        val location: IPeripheral = computer.getAvailablePeripheral(toName)
            ?: throw LuaException("Target '$toName' does not exist")

        val toStorage = ExtractorProxy.extractCCItemStorage(location.target)
            ?: throw LuaException("Target '$toName' is not an inventory")

        // Validate slots

        // Validate slots
        val actualLimit: Int = limit.orElse(Int.MAX_VALUE)
        assertBetween(fromSlot, 1, itemStorage.size(), "fromtSlot")
        if (toSlot.isPresent)
            assertBetween(toSlot.get(), 1, toStorage.size(), "toSlot")

        return if (actualLimit <= 0) 0 else InventoryDealer.moveItem(
            itemStorage,
            fromSlot - 1,
            toStorage,
            toSlot.orElse(0) - 1,
            actualLimit
        )
    }

    @LuaFunction(mainThread = true)
    @Throws(LuaException::class)
    fun pullItems(computer: IComputerAccess, fromName: String, fromSlot: Int, limit: Optional<Int>, toSlot: Optional<Int>): Int {
        // Find location to transfer to
        val location =
            computer.getAvailablePeripheral(fromName) ?: throw LuaException("Source '$fromName' does not exist")
        val fromStorage = ExtractorProxy.extractCCItemStorage(location.target)
            ?: throw LuaException("Source '$fromName' is not an inventory")

        // Validate slots
        val actualLimit = limit.orElse(Int.MAX_VALUE)
        assertBetween(fromSlot, 1, fromStorage.size(), "fromSlot")
        if (toSlot.isPresent)
            assertBetween(toSlot.get(),1, itemStorage.size(), "toSlot")
        return if (actualLimit <= 0) 0 else InventoryDealer.moveItem(
            fromStorage,
            fromSlot - 1,
            itemStorage,
            toSlot.orElse(0) - 1,
            actualLimit
        )
    }
}