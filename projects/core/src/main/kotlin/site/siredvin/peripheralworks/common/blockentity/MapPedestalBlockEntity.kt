package site.siredvin.peripheralworks.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralworks.api.IItemStackHolder
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.computercraft.peripherals.MapPedestalPeripheral
import java.util.function.Predicate

class MapPedestalBlockEntity(blockPos: BlockPos, blockState: BlockState) :
    AbstractItemPedestalBlockEntity<MapPedestalPeripheral>(
        BlockEntityTypes.MAP_PEDESTAL.get(),
        blockPos,
        blockState,
    ),
    IItemStackHolder,
    Container {

    override val itemFilter: Predicate<ItemStack> = Predicate { it.`is`(Items.MAP) || it.`is`(Items.FILLED_MAP) }

    override fun createPeripheral(side: Direction): MapPedestalPeripheral {
        return MapPedestalPeripheral(this)
    }
}
