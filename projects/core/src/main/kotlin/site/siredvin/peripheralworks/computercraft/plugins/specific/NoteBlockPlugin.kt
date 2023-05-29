package site.siredvin.peripheralworks.computercraft.plugins.specific

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.NoteBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.gameevent.GameEvent
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.util.assertBetween

class NoteBlockPlugin(private val level: Level, private val pos: BlockPos) : IPeripheralPlugin {
    companion object {
        val lowerNote: Int = NoteBlock.NOTE.possibleValues.minOf { it }
        val upperNote: Int = NoteBlock.NOTE.possibleValues.maxOf { it }
    }

    private val blockState: BlockState
        get() {
            val state = level.getBlockState(pos)
            if (state.block !is NoteBlock) {
                throw LuaException("Target block is not note block at all!")
            }
            return state
        }

    @LuaFunction(mainThread = true)
    fun getNote(): Int {
        return blockState.getValue(NoteBlock.NOTE)
    }

    @LuaFunction(mainThread = true)
    fun getInstrument(): String {
        return blockState.getValue(NoteBlock.INSTRUMENT).name.lowercase()
    }

    @LuaFunction(mainThread = true)
    fun setNote(note: Int) {
        assertBetween(note, lowerNote, upperNote, "note")
        level.setBlockAndUpdate(pos, blockState.setValue(NoteBlock.NOTE, note))
    }

    @LuaFunction(mainThread = true)
    fun setInstrument(instrument: String) {
        try {
            val instrumentValue = NoteBlockInstrument.valueOf(instrument.uppercase())
            level.setBlockAndUpdate(pos, blockState.setValue(NoteBlock.INSTRUMENT, instrumentValue))
        } catch (exc: IllegalArgumentException) {
            val allValues = NoteBlockInstrument.values().joinToString(", ") { mode -> mode.name.lowercase() }
            throw LuaException("Instrument should be one of: $allValues")
        }
    }

    @LuaFunction(mainThread = true)
    fun play() {
        level.blockEvent(pos, Blocks.NOTE_BLOCK, 0, 0)
        level.gameEvent(null, GameEvent.NOTE_BLOCK_PLAY, pos)
    }
}
