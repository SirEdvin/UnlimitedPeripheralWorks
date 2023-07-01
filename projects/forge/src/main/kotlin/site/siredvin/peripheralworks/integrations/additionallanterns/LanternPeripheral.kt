package site.siredvin.peripheralworks.integrations.additionallanterns

import com.supermartijn642.additionallanterns.LanternBlock
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin

class LanternPeripheral(private val level: Level, private val pos: BlockPos) : IPeripheralPlugin {

    override val additionalType: String
        get() = "lantern"

    @LuaFunction(mainThread = true)
    fun isEnabled(): Boolean {
        val blockState = level.getBlockState(pos)
        return blockState.getValue(LanternBlock.ON)
    }

    @LuaFunction(mainThread = true)
    fun toggle(): MethodResult {
        val state = level.getBlockState(pos)
        level.setBlock(
            pos,
            state.setValue(
                LanternBlock.ON,
                !state.getValue(LanternBlock.ON),
            ) as BlockState,
            3,
        )
        return MethodResult.of(true)
    }
}
