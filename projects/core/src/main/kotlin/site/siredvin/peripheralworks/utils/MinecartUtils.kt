package site.siredvin.peripheralworks.utils

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.vehicle.AbstractMinecart
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.phys.AABB
import site.siredvin.broccolium.modules.storage.base.api.SlottedAgnosticStorage
import site.siredvin.broccolium.modules.storage.item.ContainerWrapper
import site.siredvin.broccolium.modules.storage.item.MergedContainer

object MinecartUtils {
    private const val SEARCH_MARGIN = 0.2

    fun getSearchShape(pos: BlockPos): AABB = AABB(
        pos.x.toDouble() + SEARCH_MARGIN,
        pos.y.toDouble(),
        pos.z.toDouble() + SEARCH_MARGIN,
        (pos.x + 1).toDouble() - SEARCH_MARGIN,
        (pos.y + 1).toDouble() - SEARCH_MARGIN,
        (pos.z + 1).toDouble() - SEARCH_MARGIN,
    )

    fun getMinecarts(level: Level, pos: BlockPos): List<AbstractMinecart> = level.getEntitiesOfClass(AbstractMinecart::class.java, getSearchShape(pos)).sortedBy { it.uuid }

    fun getContainerMinecarts(level: Level, pos: BlockPos): List<AbstractMinecartContainer> = level.getEntitiesOfClass(AbstractMinecartContainer::class.java, getSearchShape(pos))

    fun minecartExtractor(level: Level, blockPos: BlockPos, blockEntity: BlockEntity?, direction: Direction?): SlottedAgnosticStorage<ItemStack, Int>? {
        val state = level.getBlockState(blockPos)
        if (!state.`is`(Blocks.POWERED_RAIL)) {
            return null
        }
        val containers = getContainerMinecarts(level, blockPos)
        if (containers.isEmpty()) {
            return null
        }
        return ContainerWrapper(MergedContainer(containers))
    }
}
