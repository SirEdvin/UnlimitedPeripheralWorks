package site.siredvin.peripheralworks.computercraft.plugins.specific

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.JukeboxBlockEntity
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.extra.plugins.PeripheralPluginUtils
import site.siredvin.peripheralium.storages.ContainerWrapper
import site.siredvin.peripheralium.storages.item.ItemStorageExtractor
import site.siredvin.peripheralium.util.representation.LuaRepresentation
import java.util.function.Predicate

class JukeboxPlugin(private val target: JukeboxBlockEntity) : IPeripheralPlugin {

    private fun assertDisc() {
        if (target.getItem(0).isEmpty) {
            throw LuaException("Disc should present in jukebox")
        }
    }

    private fun assertNoDisc() {
        if (!target.getItem(0).isEmpty) {
            throw LuaException("Jukebox should be empty")
        }
    }

    @LuaFunction(mainThread = true)
    fun getDisc(): Map<String, Any>? {
        val record = target.getItem(0)
        if (record.isEmpty) {
            return null
        }
        return LuaRepresentation.forItemStack(record)
    }

    @LuaFunction(mainThread = true)
    fun replay() {
        assertDisc()
        if (!target.isRecordPlaying) {
            target.startPlaying()
        }
    }

    @LuaFunction(mainThread = true)
    fun stop() {
        if (target.isRecordPlaying) {
            target.stopPlaying()
        }
    }

    @LuaFunction(mainThread = true)
    fun ejectDisc(computer: IComputerAccess, toName: String): MethodResult {
        assertDisc()

        val location: IPeripheral = computer.getAvailablePeripheral(toName)
            ?: throw LuaException("Target '$toName' does not exist")

        val toStorage = ItemStorageExtractor.extractItemSinkFromUnknown(target.level!!, location.target)
            ?: throw LuaException("Target '$toName' is not an item inventory")

        val stored = toStorage.storeItem(target.getItem(0))
        if (!stored.isEmpty) {
            return MethodResult.of(null, "Not enough space in target inventory")
        }

        target.removeItem(0, 1)
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun injectDisc(computer: IComputerAccess, fromName: String, itemQuery: Any?): MethodResult {
        assertNoDisc()

        val location: IPeripheral = computer.getAvailablePeripheral(fromName)
            ?: throw LuaException("Target '$fromName' does not exist")

        val fromStorage = ItemStorageExtractor.extractStorageFromUnknown(target.level!!, location.target)
            ?: throw LuaException("Target '$fromName' is not an item inventory")

        var predicate: Predicate<ItemStack> = Predicate {
            it.`is`(ItemTags.MUSIC_DISCS)
        }

        if (itemQuery != null) {
            predicate = predicate.and(PeripheralPluginUtils.itemQueryToPredicate(itemQuery))
        }

        val moved = ContainerWrapper(target).moveFrom(fromStorage, 1, takePredicate = predicate)
        if (moved == 0) {
            return MethodResult.of(null, "Cannot find disc in desired inventory")
        }
        replay()
        return MethodResult.of(true)
    }
}
