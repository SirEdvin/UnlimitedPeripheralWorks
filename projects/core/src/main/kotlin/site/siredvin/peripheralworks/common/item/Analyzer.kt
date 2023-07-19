package site.siredvin.peripheralworks.common.item

import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.common.items.DescriptiveItem

class Analyzer : DescriptiveItem(Properties().stacksTo(1)) {
    override fun use(level: Level, player: Player, interactionHand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (interactionHand == InteractionHand.OFF_HAND) {
            return super.use(level, player, interactionHand)
        }
        if (level.isClientSide) {
            val blockHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE)
            val blockEntity = level.getBlockEntity(blockHitResult.blockPos)
            if (blockEntity != null) {
                player.displayClientMessage(Component.literal("Block entity is ${blockEntity.type} (class is ${blockEntity::class.java.name}) for block ${blockHitResult.blockPos}"), false)
            } else {
                player.displayClientMessage(Component.literal("There is no entity for this block ${blockHitResult.blockPos}"), false)
            }
        }
        return InteractionResultHolder.pass(player.getItemInHand(InteractionHand.MAIN_HAND))
    }
}
