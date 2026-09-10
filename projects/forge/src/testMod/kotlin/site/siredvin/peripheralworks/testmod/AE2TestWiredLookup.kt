package site.siredvin.peripheralworks.testmod

import dan200.computercraft.api.network.wired.WiredElement
import dan200.computercraft.api.network.wired.WiredElementCapability
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level

object AE2TestWiredLookup {
    fun find(level: Level, pos: BlockPos, side: Direction?): WiredElement? = level.getCapability(WiredElementCapability.get(), pos, side ?: Direction.UP)
}
