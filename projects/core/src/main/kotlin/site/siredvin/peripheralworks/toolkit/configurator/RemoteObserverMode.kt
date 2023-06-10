package site.siredvin.peripheralworks.toolkit.configurator

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.blockentity.RemoteObserverBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.peripheralworks.data.ModTooltip

object RemoteObserverMode : ConfigurationMode {
    override val modeID: ResourceLocation = ResourceLocation(PeripheralWorksCore.MOD_ID, "remote_observer")
    override val description: Component = ModTooltip.REMOTE_OBSERVER_MODE.text

    override fun onBlockClick(configurationTarget: BlockPos, stack: ItemStack, player: Player, hit: BlockHitResult, level: Level): InteractionResultHolder<ItemStack> {
        if (level.isClientSide) {
            return InteractionResultHolder.consume(stack)
        }
        val entity = level.getBlockEntity(configurationTarget)
        if (entity !is RemoteObserverBlockEntity) {
            PeripheralWorksCore.LOGGER.error("Remote observer configuration mode renderer process $configurationTarget which is not remote observer")
            return InteractionResultHolder.consume(stack)
        }
        if (entity.blockPos == hit.blockPos) {
            player.displayClientMessage(ModText.REMOTE_OBSERVER_NOT_SELF.text, true)
            return InteractionResultHolder.consume(stack)
        }
        if (!entity.isPosApplicable(hit.blockPos)) {
            player.displayClientMessage(ModText.REMOTE_OBSERVER_TOO_FAR.text, true)
            return InteractionResultHolder.consume(stack)
        }
        if (!entity.trackedBlocksView.contains(hit.blockPos) && entity.trackedBlocksView.size >= PeripheralWorksConfig.remoteObserverMaxCapacity) {
            player.displayClientMessage(ModText.REMOTE_OBSERVER_TOO_MANY.text, true)
            return InteractionResultHolder.consume(stack)
        }
        if (entity.togglePos(hit.blockPos)) {
            player.displayClientMessage(ModText.REMOTE_OBSERVER_BLOCK_ADDED.text, true)
        } else {
            player.displayClientMessage(ModText.REMOTE_OBSERVER_BLOCK_REMOVED.text, true)
        }
        return InteractionResultHolder.consume(stack)
    }
}
