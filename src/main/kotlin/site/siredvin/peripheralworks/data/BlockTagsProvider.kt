package site.siredvin.peripheralworks.data

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.world.level.block.Block
import site.siredvin.peripheralworks.tags.BlockTags
import java.util.function.Supplier

class BlockTagsProvider(dataGenerator: FabricDataGenerator) : FabricTagProvider.BlockTagProvider(dataGenerator) {
    companion object {
        val DEFERRED_FLUID_STORAGE_BLOCKS = mutableListOf<Block>()
        val DEFERRED_FLUID_STORAGE_BLOCKS_SUP = mutableListOf<Supplier<Block>>()
    }
    override fun generateTags() {
        DEFERRED_FLUID_STORAGE_BLOCKS.forEach { this.tag(BlockTags.DEFERRED_FLUID_STORAGE).add(it) }
        DEFERRED_FLUID_STORAGE_BLOCKS_SUP.forEach { this.tag(BlockTags.DEFERRED_FLUID_STORAGE).add(it.get()) }
    }
}
