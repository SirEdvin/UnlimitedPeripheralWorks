package site.siredvin.peripheralworks.computercraft.plugins.specific

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.PoweredRailBlock
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralium.common.MinecartHelpers
import site.siredvin.peripheralium.util.MergedContainer
import site.siredvin.peripheralium.util.representation.LuaRepresentation
import site.siredvin.peripheralworks.computercraft.plugins.generic.AbstractInventoryPlugin

class PoweredRailPlugin(override val level: Level, private val pos: BlockPos): AbstractInventoryPlugin()  {

    override val additionalType: String
        get() = "poweredRail"

    private val blockState: BlockState
        get() {
            val state = level.getBlockState(pos)
            if (state.block !is PoweredRailBlock)
                throw LuaException("Target block is not note block at all!")
            return state
        }

    override val itemStorage: dan200.computercraft.shared.util.ItemStorage
        get() = dan200.computercraft.shared.util.ItemStorage.wrap(MergedContainer(MinecartHelpers.getContainerMinecarts(level, pos)))


    @LuaFunction(mainThread = true)
    fun isPowered(): Boolean {
        return blockState.getValue(PoweredRailBlock.POWERED)
    }

    @LuaFunction(mainThread = true, value = ["getMinecarts"])
    fun getMinecartsLua(): List<Map<String, Any>> {
        return MinecartHelpers.getMinecarts(level, pos).map { LuaRepresentation.forEntity(it) }
    }
}