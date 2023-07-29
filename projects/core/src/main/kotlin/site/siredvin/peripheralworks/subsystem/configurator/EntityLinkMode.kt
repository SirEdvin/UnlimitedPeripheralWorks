package site.siredvin.peripheralworks.subsystem.configurator

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import site.siredvin.peripheralworks.common.blockentity.EntityLinkBlockEntity
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
        if (!itemInOffhand.isEmpty) {
            return if (blockEntity.isSuitableUpgrade(itemInOffhand)) {
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY)
                blockEntity.injectUpgrade(itemInOffhand)
                InteractionResultHolder.success(stack)
            } else {
                // TODO: add note that item is not suitable
                InteractionResultHolder.pass(stack)
            }
        } else {
            val ejectedUpdate = blockEntity.ejectUpgrade()
            return if (!ejectedUpdate.isEmpty) {
                player.setItemInHand(InteractionHand.OFF_HAND, ejectedUpdate)
                InteractionResultHolder.success(stack)
            } else {
                // TODO: add info that nothing to eject
                InteractionResultHolder.pass(stack)
            }
        }
    }
}