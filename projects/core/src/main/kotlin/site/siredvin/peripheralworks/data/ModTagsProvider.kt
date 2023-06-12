package site.siredvin.peripheralworks.data

import net.minecraft.world.level.block.Block
import site.siredvin.peripheralium.data.blocks.ItemTagConsumer
import site.siredvin.peripheralium.data.blocks.TagConsumer
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.tags.BlockTags
import site.siredvin.peripheralworks.tags.ItemTags
import site.siredvin.peripheralworks.xplat.ModBlocksReference
import java.util.function.Supplier

object ModTagsProvider {
    private val DEFAULT_PERIPHERAL_PROXY_BLOCKED_BLOCKS = listOf(
        Blocks.PERIPHERAL_PROXY,
        Supplier { ModBlocksReference.get().wiredModem },
        Supplier { ModBlocksReference.get().cable },
    )

    @JvmStatic
    fun blockTags(consumer: TagConsumer<Block>) {
        DEFAULT_PERIPHERAL_PROXY_BLOCKED_BLOCKS.forEach { consumer.tag(BlockTags.PERIPHERAL_PROXY_FORBIDDEN).add(it.get()) }
    }

    @JvmStatic
    fun itemTags(consumer: ItemTagConsumer) {
        DEFAULT_PERIPHERAL_PROXY_BLOCKED_BLOCKS.forEach { consumer.tag(ItemTags.PERIPHERAL_PROXY_FORBIDDEN).add(it.get().asItem()) }
    }
}
