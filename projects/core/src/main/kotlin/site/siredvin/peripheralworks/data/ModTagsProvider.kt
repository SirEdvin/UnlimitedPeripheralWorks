package site.siredvin.peripheralworks.data

import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.block.Block
import site.siredvin.peripheralium.data.blocks.ItemTagConsumer
import site.siredvin.peripheralium.data.blocks.TagConsumer
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.tags.BlockTags
import site.siredvin.peripheralworks.tags.EntityTags
import site.siredvin.peripheralworks.tags.ItemTags
import site.siredvin.peripheralworks.xplat.ModBlocksReference
import java.util.function.Supplier

object ModTagsProvider {
    private val DEFAULT_PERIPHERAL_PROXY_BLOCKED_BLOCKS = listOf(
        Blocks.PERIPHERAL_PROXY,
        Supplier { ModBlocksReference.get().wiredModem },
        Supplier { ModBlocksReference.get().cable },
    )

    private val DEFAULT_MIMIC_BLOCKED_BLOCKS = listOf(
        Supplier { net.minecraft.world.level.block.Blocks.OAK_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.SPRUCE_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.BIRCH_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.ACACIA_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.CHERRY_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.JUNGLE_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.DARK_OAK_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.MANGROVE_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.BAMBOO_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.OAK_WALL_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.SPRUCE_WALL_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.BIRCH_WALL_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.ACACIA_WALL_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.CHERRY_WALL_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.JUNGLE_WALL_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.DARK_OAK_WALL_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.MANGROVE_WALL_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.BAMBOO_WALL_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.OAK_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.SPRUCE_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.BIRCH_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.ACACIA_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.CHERRY_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.JUNGLE_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.DARK_OAK_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.CRIMSON_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.WARPED_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.MANGROVE_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.BAMBOO_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.OAK_WALL_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.SPRUCE_WALL_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.BIRCH_WALL_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.ACACIA_WALL_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.CHERRY_WALL_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.JUNGLE_WALL_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.DARK_OAK_WALL_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.MANGROVE_WALL_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.CRIMSON_WALL_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.WARPED_WALL_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.BAMBOO_WALL_HANGING_SIGN },
        Supplier { net.minecraft.world.level.block.Blocks.WHITE_BED },
        Supplier { net.minecraft.world.level.block.Blocks.ORANGE_BED },
        Supplier { net.minecraft.world.level.block.Blocks.MAGENTA_BED },
        Supplier { net.minecraft.world.level.block.Blocks.LIGHT_BLUE_BED },
        Supplier { net.minecraft.world.level.block.Blocks.YELLOW_BED },
        Supplier { net.minecraft.world.level.block.Blocks.LIME_BED },
        Supplier { net.minecraft.world.level.block.Blocks.PINK_BED },
        Supplier { net.minecraft.world.level.block.Blocks.GRAY_BED },
        Supplier { net.minecraft.world.level.block.Blocks.LIGHT_GRAY_BED },
        Supplier { net.minecraft.world.level.block.Blocks.CYAN_BED },
        Supplier { net.minecraft.world.level.block.Blocks.PURPLE_BED },
        Supplier { net.minecraft.world.level.block.Blocks.BLUE_BED },
        Supplier { net.minecraft.world.level.block.Blocks.BROWN_BED },
        Supplier { net.minecraft.world.level.block.Blocks.GREEN_BED },
        Supplier { net.minecraft.world.level.block.Blocks.RED_BED },
        Supplier { net.minecraft.world.level.block.Blocks.BLACK_BED },
        Supplier { net.minecraft.world.level.block.Blocks.CHEST },
        Supplier { net.minecraft.world.level.block.Blocks.ENDER_CHEST },
        Supplier { net.minecraft.world.level.block.Blocks.TRAPPED_CHEST },
        Supplier { net.minecraft.world.level.block.Blocks.SHULKER_BOX },
        Supplier { net.minecraft.world.level.block.Blocks.WHITE_SHULKER_BOX },
        Supplier { net.minecraft.world.level.block.Blocks.ORANGE_SHULKER_BOX },
        Supplier { net.minecraft.world.level.block.Blocks.MAGENTA_SHULKER_BOX },
        Supplier { net.minecraft.world.level.block.Blocks.LIGHT_BLUE_SHULKER_BOX },
        Supplier { net.minecraft.world.level.block.Blocks.YELLOW_SHULKER_BOX },
        Supplier { net.minecraft.world.level.block.Blocks.LIME_SHULKER_BOX },
        Supplier { net.minecraft.world.level.block.Blocks.PINK_SHULKER_BOX },
        Supplier { net.minecraft.world.level.block.Blocks.GRAY_SHULKER_BOX },
        Supplier { net.minecraft.world.level.block.Blocks.LIGHT_GRAY_SHULKER_BOX },
        Supplier { net.minecraft.world.level.block.Blocks.CYAN_SHULKER_BOX },
        Supplier { net.minecraft.world.level.block.Blocks.PURPLE_SHULKER_BOX },
        Supplier { net.minecraft.world.level.block.Blocks.BLUE_SHULKER_BOX },
        Supplier { net.minecraft.world.level.block.Blocks.BROWN_SHULKER_BOX },
        Supplier { net.minecraft.world.level.block.Blocks.GREEN_SHULKER_BOX },
        Supplier { net.minecraft.world.level.block.Blocks.RED_SHULKER_BOX },
        Supplier { net.minecraft.world.level.block.Blocks.BLACK_SHULKER_BOX },
        Supplier { net.minecraft.world.level.block.Blocks.SPRUCE_DOOR },
        Supplier { net.minecraft.world.level.block.Blocks.BIRCH_DOOR },
        Supplier { net.minecraft.world.level.block.Blocks.JUNGLE_DOOR },
        Supplier { net.minecraft.world.level.block.Blocks.ACACIA_DOOR },
        Supplier { net.minecraft.world.level.block.Blocks.CHERRY_DOOR },
        Supplier { net.minecraft.world.level.block.Blocks.DARK_OAK_DOOR },
        Supplier { net.minecraft.world.level.block.Blocks.MANGROVE_DOOR },
        Supplier { net.minecraft.world.level.block.Blocks.BAMBOO_DOOR },
        Supplier { net.minecraft.world.level.block.Blocks.IRON_DOOR },
        Supplier { net.minecraft.world.level.block.Blocks.NETHER_PORTAL },
        Supplier { net.minecraft.world.level.block.Blocks.END_GATEWAY },
        Supplier { net.minecraft.world.level.block.Blocks.END_PORTAL },
        Supplier { net.minecraft.world.level.block.Blocks.VOID_AIR },
        Supplier { net.minecraft.world.level.block.Blocks.STRUCTURE_VOID },
        Supplier { net.minecraft.world.level.block.Blocks.BUBBLE_COLUMN },
        Supplier { net.minecraft.world.level.block.Blocks.SKELETON_SKULL },
        Supplier { net.minecraft.world.level.block.Blocks.SKELETON_WALL_SKULL },
        Supplier { net.minecraft.world.level.block.Blocks.WITHER_SKELETON_SKULL },
        Supplier { net.minecraft.world.level.block.Blocks.WITHER_SKELETON_WALL_SKULL },
        Supplier { net.minecraft.world.level.block.Blocks.ZOMBIE_HEAD },
        Supplier { net.minecraft.world.level.block.Blocks.ZOMBIE_WALL_HEAD },
        Supplier { net.minecraft.world.level.block.Blocks.PLAYER_HEAD },
        Supplier { net.minecraft.world.level.block.Blocks.PLAYER_WALL_HEAD },
        Supplier { net.minecraft.world.level.block.Blocks.CREEPER_HEAD },
        Supplier { net.minecraft.world.level.block.Blocks.CREEPER_WALL_HEAD },
        Supplier { net.minecraft.world.level.block.Blocks.DRAGON_HEAD },
        Supplier { net.minecraft.world.level.block.Blocks.DRAGON_WALL_HEAD },
        Supplier { net.minecraft.world.level.block.Blocks.PIGLIN_HEAD },
        Supplier { net.minecraft.world.level.block.Blocks.PIGLIN_WALL_HEAD },
        Supplier { net.minecraft.world.level.block.Blocks.PISTON_HEAD },
        Supplier { net.minecraft.world.level.block.Blocks.MOVING_PISTON },
        Supplier { net.minecraft.world.level.block.Blocks.WHITE_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.ORANGE_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.MAGENTA_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.LIGHT_BLUE_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.YELLOW_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.LIME_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.PINK_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.GRAY_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.LIGHT_GRAY_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.CYAN_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.PURPLE_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.BLUE_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.BROWN_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.GREEN_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.RED_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.BLACK_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.WHITE_WALL_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.ORANGE_WALL_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.MAGENTA_WALL_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.LIGHT_BLUE_WALL_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.YELLOW_WALL_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.LIME_WALL_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.PINK_WALL_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.GRAY_WALL_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.LIGHT_GRAY_WALL_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.CYAN_WALL_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.PURPLE_WALL_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.BLUE_WALL_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.BROWN_WALL_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.GREEN_WALL_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.RED_WALL_BANNER },
        Supplier { net.minecraft.world.level.block.Blocks.BLACK_WALL_BANNER },
        Blocks.FLEXIBLE_REALITY_ANCHOR,
        Blocks.FLEXIBLE_STATUE,
    )

    @JvmStatic
    fun blockTags(consumer: TagConsumer<Block>) {
        DEFAULT_PERIPHERAL_PROXY_BLOCKED_BLOCKS.forEach { consumer.tag(BlockTags.PERIPHERAL_PROXY_FORBIDDEN).add(it.get()) }
        DEFAULT_MIMIC_BLOCKED_BLOCKS.forEach { consumer.tag(BlockTags.REALITY_FORGER_FORBIDDEN).add(it.get()) }
        consumer.tag(BlockTags.CCT_PERIPHERAL_HUB_IGNORE).add(Blocks.PERIPHERAL_PROXY.get())
    }

    @JvmStatic
    fun itemTags(consumer: ItemTagConsumer) {
        DEFAULT_PERIPHERAL_PROXY_BLOCKED_BLOCKS.forEach { consumer.tag(ItemTags.PERIPHERAL_PROXY_FORBIDDEN).add(it.get().asItem()) }
        DEFAULT_MIMIC_BLOCKED_BLOCKS.forEach { consumer.tag(ItemTags.REALITY_FORGER_FORBIDDEN).add(it.get().asItem()) }
    }

    @JvmStatic
    fun entityTypeTags(consumer: TagConsumer<EntityType<*>>) {
        consumer.tag(EntityTags.LINK_BLOCKLIST).add(
            EntityType.ENDER_DRAGON,
            EntityType.WITHER,
            EntityType.WARDEN,
            EntityType.PLAYER,
        )
    }
}
