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
import site.siredvin.peripheralium.xplat.PeripheraliumPlatform
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.blockentity.PeripheralProxyBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig

object PeripheralProxyMode : ConfigurationMode {
    override val modeID: ResourceLocation = ResourceLocation(PeripheralWorksCore.MOD_ID, "peripheral_proxy")
    override val description: Component = text(PeripheralWorksCore.MOD_ID, "peripheral_proxy_mode")

    override fun onBlockClick(configurationTarget: BlockPos, stack: ItemStack, player: Player, hit: BlockHitResult, level: Level): InteractionResultHolder<ItemStack> {
        if (level.isClientSide) {
            return InteractionResultHolder.consume(stack)
        }
        val entity = level.getBlockEntity(configurationTarget)
        if (entity !is PeripheralProxyBlockEntity) {
            PeripheralWorksCore.LOGGER.error("Peripheral proxy configuration mode renderer process $configurationTarget which is not peripheral proxy")
            return InteractionResultHolder.consume(stack)
        }
        if (entity.blockPos == hit.blockPos) {
            player.displayClientMessage(text(PeripheralWorksCore.MOD_ID, "peripheral_proxy_not_self"), true)
            return InteractionResultHolder.consume(stack)
        }
        if (!entity.isPosApplicable(hit.blockPos)) {
            player.displayClientMessage(text(PeripheralWorksCore.MOD_ID, "peripheral_proxy_too_far"), true)
            return InteractionResultHolder.consume(stack)
        }
        if (!entity.remotePeripherals.contains(hit.blockPos) && entity.remotePeripherals.size >= PeripheralWorksConfig.peripheralProxyMaxCapacity) {
            player.displayClientMessage(text(PeripheralWorksCore.MOD_ID, "peripheral_proxy_too_many"), true)
            return InteractionResultHolder.consume(stack)
        }
        val targetPeripheral = PeripheraliumPlatform.getPeripheral(level, hit.blockPos, hit.direction)
        if (targetPeripheral == null && !entity.containsPos(hit.blockPos)) {
            player.displayClientMessage(text(PeripheralWorksCore.MOD_ID, "peripheral_proxy_is_not_peripheral"), true)
            return InteractionResultHolder.consume(stack)
        }
        if (targetPeripheral != null) {
            if (entity.togglePos(hit.blockPos, hit.direction, targetPeripheral)) {
                player.displayClientMessage(
                    text(PeripheralWorksCore.MOD_ID, "peripheral_proxy_block_track_added"),
                    true,
                )
            } else {
                player.displayClientMessage(
                    text(PeripheralWorksCore.MOD_ID, "peripheral_proxy_block_track_removed"),
                    true,
                )
            }
        } else if (entity.removePosToTrack(hit.blockPos)) {
            player.displayClientMessage(
                text(PeripheralWorksCore.MOD_ID, "peripheral_proxy_block_track_removed"),
                true,
            )
        }
        return InteractionResultHolder.consume(stack)
    }
}
