package site.siredvin.peripheralworks.toolkit.configurator

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import site.siredvin.peripheralium.util.text
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.blockentity.RemoteObserverBlockEntity

object RemoteObserverMode: ConfigurationMode {
    override val modeID: ResourceLocation = ResourceLocation(PeripheralWorksCore.MOD_ID, "remote_observer")
    override val description: Component = text(PeripheralWorksCore.MOD_ID, "remote_observer_mode")

    override fun onBlockClick(configurationTarget: BlockPos, stack: ItemStack, player: Player, hit: BlockHitResult, level: Level): InteractionResultHolder<ItemStack> {
        if (level.isClientSide)
            return InteractionResultHolder.consume(stack)
        val entity = level.getBlockEntity(configurationTarget)
        if (entity !is RemoteObserverBlockEntity) {
            PeripheralWorksCore.LOGGER.error("Remote observer configuration mode renderer process $configurationTarget which is not remote observer")
            return InteractionResultHolder.consume(stack)
        }
        if (!entity.isPosApplicable(hit.blockPos)) {
            player.displayClientMessage(text(PeripheralWorksCore.MOD_ID, "remote_observer_too_far"), true)
            return InteractionResultHolder.consume(stack)
        }
        if (entity.togglePos(hit.blockPos)) {
            player.displayClientMessage(text(PeripheralWorksCore.MOD_ID, "remote_observer_block_track_added"), true)
        } else {
            player.displayClientMessage(text(PeripheralWorksCore.MOD_ID, "remote_observer_block_track_removed"), true)
        }
        return InteractionResultHolder.consume(stack)
    }
}