package site.siredvin.peripheralworks.data

import net.minecraft.data.loot.LootTableProvider
import net.minecraft.data.loot.LootTableSubProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import site.siredvin.peripheralium.data.blocks.LootTableHelper
import site.siredvin.peripheralworks.common.setup.Blocks
import java.util.function.BiConsumer

object ModLootTableProvider {
    fun getTables(): List<LootTableProvider.SubProviderEntry> {
        return listOf(
            LootTableProvider.SubProviderEntry({
                LootTableSubProvider {
                    registerBlocks(it)
                }
            }, LootContextParamSets.BLOCK),
        )
    }

    fun registerBlocks(consumer: BiConsumer<ResourceLocation, LootTable.Builder>) {
        LootTableHelper.dropSelf(consumer, Blocks.ULTIMATE_SENSOR)
        LootTableHelper.dropSelf(consumer, Blocks.UNIVERSAL_SCANNER)
        LootTableHelper.dropSelf(consumer, Blocks.PERIPHERAL_CASING)
        LootTableHelper.dropSelf(consumer, Blocks.ITEM_PEDESTAL)
        LootTableHelper.dropSelf(consumer, Blocks.MAP_PEDESTAL)
        LootTableHelper.dropSelf(consumer, Blocks.DISPLAY_PEDESTAL)
        LootTableHelper.dropSelf(consumer, Blocks.REMOTE_OBSERVER)
        LootTableHelper.dropSelf(consumer, Blocks.PERIPHERAL_PROXY)
        LootTableHelper.dropSelf(consumer, Blocks.REALITY_FORGER)
        LootTableHelper.dropSelf(consumer, Blocks.RECIPE_REGISTRY)
        LootTableHelper.dropSelf(consumer, Blocks.INFORMATIVE_REGISTRY)
    }
}
