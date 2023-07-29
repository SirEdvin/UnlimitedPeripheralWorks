package site.siredvin.peripheralworks.subsystem.configurator

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import site.siredvin.peripheralworks.common.blockentity.EntityLinkBlockEntity
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.peripheralworks.data.ModTooltip
import site.siredvin.peripheralworks.utils.modId

object EntityLinkMode: ConfigurationMode {
    override val modeID: ResourceLocation
        get() = modId("entity_link")
    override val description: Component
        get() = ModTooltip.ENTITY_LINK_MODE.text

    override fun onBlockClick(
        configurationTarget: BlockPos,
        stack: ItemStack,
        player: Player,
        hit: BlockHitResult,
        level: Level
    ): InteractionResultHolder<ItemStack> {
        val itemInOffhand = player.getItemInHand(InteractionHand.OFF_HAND)
        val blockEntity = level.getBlockEntity(configurationTarget) as? EntityLinkBlockEntity ?: return InteractionResultHolder.pass(stack)
        if (player.pose != Pose.CROUCHING) {
            val message = if (blockEntity.upgrades.scanner) {
                ModText.ENTITY_LINK_UPGRADES.text.append("\n")
                    .append("  ").append(ModText.ENTITY_LINK_UPGRADE_SCANNER.text)
            } else {
                ModText.ENTITY_LINK_DOES_NOT_HAVE_UPGRADES.text
            }
            if (level.isClientSide)
                player.sendSystemMessage(message)
            return InteractionResultHolder.success(stack)
        }
        if (!itemInOffhand.isEmpty) {
            return if (blockEntity.isSuitableUpgrade(itemInOffhand)) {
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY)
                blockEntity.injectUpgrade(itemInOffhand)
                InteractionResultHolder.success(stack)
            } else {
                if (level.isClientSide)
                    player.displayClientMessage(ModText.ITEM_IS_NOT_SUITABLE_FOR_UPGRADE.text, false)
                InteractionResultHolder.success(stack)
            }
        }
        val ejectedUpdate = blockEntity.ejectUpgrade()
        return if (!ejectedUpdate.isEmpty) {
            player.setItemInHand(InteractionHand.OFF_HAND, ejectedUpdate)
            InteractionResultHolder.success(stack)
        } else {
            if (level.isClientSide)
                player.displayClientMessage(ModText.ENTITY_LINK_DOES_NOT_HAVE_UPGRADES.text, false)
            InteractionResultHolder.success(stack)
        }
    }
}