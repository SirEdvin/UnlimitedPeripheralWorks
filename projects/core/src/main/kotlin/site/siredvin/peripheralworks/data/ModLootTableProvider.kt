package site.siredvin.peripheralworks.data

import net.minecraft.data.loot.LootTableProvider
import net.minecraft.data.loot.LootTableSubProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import site.siredvin.peripheralium.data.blocks.LootTableHelper
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.xplat.ModPlatform
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
        val lootTable = LootTableHelper(ModPlatform.holder)
        lootTable.dropSelf(consumer, Blocks.ULTIMATE_SENSOR)
        lootTable.dropSelf(consumer, Blocks.UNIVERSAL_SCANNER)
        lootTable.dropSelf(consumer, Blocks.PERIPHERAL_CASING)
        lootTable.dropSelf(consumer, Blocks.ITEM_PEDESTAL)
        lootTable.dropSelf(consumer, Blocks.MAP_PEDESTAL)
        lootTable.dropSelf(consumer, Blocks.DISPLAY_PEDESTAL)
        lootTable.dropSelf(consumer, Blocks.REMOTE_OBSERVER)
        lootTable.dropSelf(consumer, Blocks.PERIPHERAL_PROXY)
        lootTable.dropSelf(consumer, Blocks.REALITY_FORGER)
        lootTable.dropSelf(consumer, Blocks.RECIPE_REGISTRY)
        lootTable.dropSelf(consumer, Blocks.INFORMATIVE_REGISTRY)
        lootTable.dropSelf(consumer, Blocks.STATUE_WORKBENCH)
        lootTable.dropSelf(consumer, Blocks.ENTITY_LINK)
        lootTable.computedDrop(Blocks.FLEXIBLE_REALITY_ANCHOR)
        lootTable.computedDrop(Blocks.FLEXIBLE_STATUE)
        lootTable.validate()
    }
}
