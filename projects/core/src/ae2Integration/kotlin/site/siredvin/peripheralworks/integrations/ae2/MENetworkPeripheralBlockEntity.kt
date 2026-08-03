package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.networking.GridFlags
import appeng.api.networking.IManagedGridNode
import appeng.blockentity.grid.AENetworkBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.state.BlockState

class MENetworkPeripheralBlockEntity(pos: BlockPos, state: BlockState) : AENetworkBlockEntity(Registration.ME_NETWORK_PERIPHERAL_BLOCK_ENTITY.get(), pos, state) {

    override fun createMainNode(): IManagedGridNode = super.createMainNode().setFlags(GridFlags.REQUIRE_CHANNEL)

    override fun getItemFromBlockEntity(): Item = Registration.ME_NETWORK_PERIPHERAL.get().asItem()
}
