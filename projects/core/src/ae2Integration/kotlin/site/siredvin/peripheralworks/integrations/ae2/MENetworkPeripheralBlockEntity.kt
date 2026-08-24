package site.siredvin.peripheralworks.integrations.ae2

// Inspired by Advanced Peripherals' MeBridgeEntity by SirEndii, later updated by zyxkad:
// https://github.com/IntelligenceModding/AdvancedPeripherals/blob/9f0101b22bd66418d2114f2e08fc61a11b1b77cb/src/main/java/de/srendi/advancedperipherals/common/blocks/blockentities/MeBridgeEntity.java

import appeng.api.networking.GridFlags
import appeng.api.networking.IManagedGridNode
import appeng.api.networking.storage.IStorageWatcherNode
import appeng.blockentity.grid.AENetworkBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.broccolium.modules.base.block.FacingBlockEntityBlock
import java.util.function.Supplier

class MENetworkPeripheralBlock(
    blockEntityType: Supplier<BlockEntityType<MENetworkPeripheralBlockEntity>>,
    properties: BlockBehaviour.Properties,
) : FacingBlockEntityBlock<MENetworkPeripheralBlockEntity>(blockEntityType, false, false, properties)

class MENetworkPeripheralBlockEntity(pos: BlockPos, state: BlockState) : AENetworkBlockEntity(Registration.ME_NETWORK_PERIPHERAL_BLOCK_ENTITY.get(), pos, state) {

    val subscriptionTracker = AE2StorageSubscriptionTracker()
    private val subscriptionWatcher = AE2StorageWatcherNode(subscriptionTracker) { mainNode.grid }

    init {
        mainNode.addService(IStorageWatcherNode::class.java, subscriptionWatcher)
        subscriptionTracker.onDefinitionsChanged = ::setChanged
        subscriptionTracker.onActivityChanged = { level?.server?.execute(subscriptionWatcher::refresh) }
    }

    override fun createMainNode(): IManagedGridNode = super.createMainNode().setFlags(GridFlags.REQUIRE_CHANNEL)

    override fun loadTag(tag: CompoundTag) {
        super.loadTag(tag)
        if (tag.loadAE2StorageSubscriptions(subscriptionTracker)) setChanged()
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        tag.putAE2StorageSubscriptions(subscriptionTracker)
    }

    override fun onChunkUnloaded() {
        subscriptionWatcher.stop()
        super.onChunkUnloaded()
    }

    override fun setRemoved() {
        subscriptionWatcher.stop()
        super.setRemoved()
    }

    override fun getItemFromBlockEntity(): Item = Registration.ME_NETWORK_PERIPHERAL.get().asItem()
}
