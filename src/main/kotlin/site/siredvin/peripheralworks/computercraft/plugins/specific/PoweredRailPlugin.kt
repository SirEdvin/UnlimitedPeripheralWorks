package site.siredvin.peripheralworks.computercraft.plugins.specific

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.PoweredRailBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.RailShape
import net.minecraft.world.phys.Vec3
import site.siredvin.peripheralium.common.MinecartHelpers
import site.siredvin.peripheralium.extra.plugins.AbstractInventoryPlugin
import site.siredvin.peripheralium.util.MergedContainer
import site.siredvin.peripheralium.util.representation.LuaRepresentation
import java.util.*

class PoweredRailPlugin(override val level: Level, private val pos: BlockPos) : AbstractInventoryPlugin() {

    companion object {
        const val PUSH_POWER = 5.0
    }

    private val blockState: BlockState
        get() {
            val state = level.getBlockState(pos)
            if (state.block !is PoweredRailBlock) {
                throw LuaException("Target block is not note block at all!")
            }
            return state
        }

    override val itemStorage: dan200.computercraft.shared.util.ItemStorage
        get() = dan200.computercraft.shared.util.ItemStorage.wrap(MergedContainer(MinecartHelpers.getContainerMinecarts(level, pos)))

    @LuaFunction(mainThread = true)
    fun isPowered(): Boolean {
        return blockState.getValue(PoweredRailBlock.POWERED)
    }

    @LuaFunction(mainThread = true)
    fun pushMinecarts(reverse: Optional<Boolean>) {
        val orientation = blockState.getValue(PoweredRailBlock.SHAPE)!!
        val isReversed = reverse.orElse(false)
        Direction.NORTH
        val moveVector = when (orientation) {
            RailShape.NORTH_SOUTH -> Vec3(0.0, 0.0, if (isReversed) PUSH_POWER else -PUSH_POWER)
            RailShape.EAST_WEST -> Vec3(if (isReversed) -PUSH_POWER else PUSH_POWER, 0.0, 0.0)
            RailShape.ASCENDING_EAST -> Vec3(if (isReversed) -PUSH_POWER else PUSH_POWER, 0.0, 0.0)
            RailShape.ASCENDING_WEST -> Vec3(if (isReversed) PUSH_POWER else -PUSH_POWER, 0.0, 0.0)
            RailShape.ASCENDING_NORTH -> Vec3(0.0, 0.0, if (isReversed) PUSH_POWER else -PUSH_POWER)
            RailShape.ASCENDING_SOUTH -> Vec3(0.0, 0.0, if (isReversed) -PUSH_POWER else PUSH_POWER)
            RailShape.SOUTH_EAST -> if (isReversed) Vec3(0.0, 0.0, PUSH_POWER) else Vec3(PUSH_POWER, 0.0, 0.0)
            RailShape.SOUTH_WEST -> if (isReversed) Vec3(0.0, 0.0, PUSH_POWER) else Vec3(-PUSH_POWER, 0.0, 0.0)
            RailShape.NORTH_WEST -> if (isReversed) Vec3(0.0, 0.0, -PUSH_POWER) else Vec3(-PUSH_POWER, 0.0, 0.0)
            RailShape.NORTH_EAST -> if (isReversed) Vec3(0.0, 0.0, -PUSH_POWER) else Vec3(PUSH_POWER, 0.0, 0.0)
        }
        MinecartHelpers.getMinecarts(level, pos).filter { it.deltaMovement.length() == 0.0 }.forEach {
            it.deltaMovement = moveVector
        }
    }

    @LuaFunction(mainThread = true, value = ["getMinecarts"])
    fun getMinecartsLua(): List<Map<String, Any>> {
        return MinecartHelpers.getMinecarts(level, pos).map { LuaRepresentation.forEntity(it) }
    }
}
