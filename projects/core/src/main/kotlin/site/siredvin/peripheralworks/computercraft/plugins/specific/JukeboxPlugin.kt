package site.siredvin.peripheralworks.computercraft.plugins.specific

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import net.minecraft.core.Holder
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.JukeboxSong
import net.minecraft.world.level.block.entity.JukeboxBlockEntity
import site.siredvin.broccolium.modules.storage.item.AgnosticItemStorageLookup
import site.siredvin.broccolium.modules.storage.item.ContainerWrapper
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation
import site.siredvin.tweakium.modules.plugins.PeripheralPluginUtils
import java.util.function.Predicate

class JukeboxPlugin(private val target: JukeboxBlockEntity) : IPeripheralPlugin {

    private fun assertDisc() {
        if (target.theItem.isEmpty) {
            throw LuaException("Disc should present in jukebox")
        }
    }

    private fun assertNoDisc() {
        if (!target.theItem.isEmpty) {
            throw LuaException("Jukebox should be empty")
        }
    }

    @LuaFunction(mainThread = true)
    fun getDisc(): Map<String, Any>? {
        val record = target.theItem
        if (record.isEmpty) {
            return null
        }
        return LuaRepresentation.forItemStack(record)
    }

    @LuaFunction(mainThread = true)
    fun replay() {
        assertDisc()
        if (!target.jukeboxSongPlayer.isPlaying) {
            JukeboxSong.fromStack(target.level!!.registryAccess(), target.theItem)
                .ifPresent { holder: Holder<JukeboxSong> ->
                    target.jukeboxSongPlayer.play(
                        target.level!!,
                        holder,
                    )
                }
        }
    }

    @LuaFunction(mainThread = true)
    fun stop() {
        if (target.jukeboxSongPlayer.isPlaying) {
            target.jukeboxSongPlayer.stop(target.level!!, target.blockState)
        }
    }

    @LuaFunction(mainThread = true)
    fun ejectDisc(computer: IComputerAccess, toName: String): MethodResult {
        assertDisc()

        val location: IPeripheral = computer.getAvailablePeripheral(toName)
            ?: throw LuaException("Target '$toName' does not exist")

        val toStorage = AgnosticItemStorageLookup.extractItemSinkFromUnknown(target.level!!, location.target)
            ?: throw LuaException("Target '$toName' is not an item inventory")

        val stored = toStorage.storeItem(target.theItem)
        if (!stored.isEmpty) {
            return MethodResult.of(null, "Not enough space in target inventory")
        }

        target.theItem = ItemStack.EMPTY
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun injectDisc(computer: IComputerAccess, fromName: String, itemQuery: Any?): MethodResult {
        assertNoDisc()

        val location: IPeripheral = computer.getAvailablePeripheral(fromName)
            ?: throw LuaException("Target '$fromName' does not exist")

        val fromStorage = AgnosticItemStorageLookup.extractStorageFromUnknown(target.level!!, location.target)
            ?: throw LuaException("Target '$fromName' is not an item inventory")

        var predicate: Predicate<ItemStack> = Predicate {
            JukeboxSong.fromStack(target.level!!.registryAccess(), it).isPresent
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
