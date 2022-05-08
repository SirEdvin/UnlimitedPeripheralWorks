package site.siredvin.peripheralworks.computercraft.plugins.specific

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.RecordItem
import net.minecraft.world.level.block.JukeboxBlock
import net.minecraft.world.level.block.entity.JukeboxBlockEntity
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.common.ExtractorProxy
import site.siredvin.peripheralium.util.representation.LuaRepresentation

class JukeboxPlugin(private val target: JukeboxBlockEntity): IPeripheralPlugin {
    override val additionalType: String
        get() = "jukebox"

    private fun assertDisc() {
        if (target.record.isEmpty)
            throw LuaException("Disc should present in jukebox")
    }

    private fun assertNoDisc() {
        if (!target.record.isEmpty)
            throw LuaException("Jukebox should be empty")
    }

    @LuaFunction(mainThread = true)
    fun getDisc(): Map<String, Any>? {
        val record = target.record
        if (record.isEmpty)
            return null
        return LuaRepresentation.forItemStack(record)
    }

    @LuaFunction(mainThread = true)
    fun replay() {
        assertDisc()
        target.level!!.levelEvent(null, 1010, target.blockPos, Item.getId(target.record.item))
    }

    @LuaFunction(mainThread = true)
    fun stop() {
        target.level!!.levelEvent(1010, target.blockPos, 0)
    }

    @LuaFunction(mainThread = true)
    fun ejectDisc(computer: IComputerAccess, toName: String): MethodResult {
        assertDisc()

        val location: IPeripheral = computer.getAvailablePeripheral(toName)
            ?: throw LuaException("Target '$toName' does not exist")

        val toStorage = ExtractorProxy.extractItemStorage(target.level!!, location.target)
            ?: throw LuaException("Target '$toName' is not an item inventory")

        Transaction.openOuter().use {
            val amount = toStorage.insert(ItemVariant.of(target.record), 1, it)
            if (amount != 1L) {
                it.abort()
                return MethodResult.of(null, "Not enough space in target inventory")
            }
            target.clearContent()
            it.commit()
        }
        target.level!!.setBlockAndUpdate(target.blockPos, target.blockState.setValue(JukeboxBlock.HAS_RECORD, false))
        stop()
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun injectDisc(computer: IComputerAccess, fromName: String, targetItem: String): MethodResult {
        assertNoDisc()

        val location: IPeripheral = computer.getAvailablePeripheral(fromName)
            ?: throw LuaException("Target '$fromName' does not exist")

        val fromStorage = ExtractorProxy.extractItemStorage(target.level!!, location.target)
            ?: throw LuaException("Target '$fromName' is not an item inventory")

        val item = Registry.ITEM.get(ResourceLocation(targetItem))
        if (item == Items.AIR)
            throw LuaException("Cannot find item $targetItem")
        if (item !is RecordItem)
            throw LuaException("Item should be a valid disc item")

        Transaction.openOuter().use {
            val amount = fromStorage.extract(ItemVariant.of(item), 1, it)
            if (amount != 1L) {
                it.abort()
                return MethodResult.of(null, "Cannot find disc in desired inventory")
            }
            target.record = item.defaultInstance
            it.commit()
        }
        target.level!!.setBlockAndUpdate(target.blockPos, target.blockState.setValue(JukeboxBlock.HAS_RECORD, true))
        replay()
        return MethodResult.of(true)
    }
}