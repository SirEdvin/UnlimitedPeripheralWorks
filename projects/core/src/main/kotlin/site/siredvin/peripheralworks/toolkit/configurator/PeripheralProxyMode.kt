package site.siredvin.peripheralworks.toolkit.configurator

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import site.siredvin.peripheralium.xplat.PeripheraliumPlatform
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.blockentity.PeripheralProxyBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.peripheralworks.data.ModTooltip

object PeripheralProxyMode : ConfigurationMode {
    override val modeID: ResourceLocation = ResourceLocation(PeripheralWorksCore.MOD_ID, "peripheral_proxy")
    override val description: Component = ModTooltip.PERIPHERAL_PROXY_MODE.text

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
            player.displayClientMessage(ModText.PERIPHERAL_PROXY_NOT_SELF.text, true)
            return InteractionResultHolder.consume(stack)
        }
        if (!entity.isPosApplicable(hit.blockPos)) {
            player.displayClientMessage(ModText.PERIPHERAL_PROXY_TOO_FAR.text, true)
            return InteractionResultHolder.consume(stack)
        }
        if (!entity.remotePeripherals.contains(hit.blockPos) && entity.remotePeripherals.size >= PeripheralWorksConfig.peripheralProxyMaxCapacity) {
            player.displayClientMessage(ModText.PERIPHERAL_PROXY_TOO_MANY.text, true)
            return InteractionResultHolder.consume(stack)
        }
        val targetPeripheral = PeripheraliumPlatform.getPeripheral(level, hit.blockPos, hit.direction)
        if (targetPeripheral == null && !entity.containsPos(hit.blockPos)) {
            player.displayClientMessage(ModText.PERIPHERAL_PROXY_IS_NOT_A_PERIPHERAL.text, true)
            return InteractionResultHolder.consume(stack)
        }
        if (targetPeripheral != null) {
            if (entity.togglePos(hit.blockPos, hit.direction, targetPeripheral)) {
                player.displayClientMessage(
                    ModText.PERIPHERAL_PROXY_BLOCK_ADDED.text,
                    true,
                )
            } else {
                player.displayClientMessage(
                    ModText.PERIPHERAL_PROXY_BLOCK_REMOVED.text,
                    true,
                )
            }
        } else if (entity.removePosToTrack(hit.blockPos)) {
            player.displayClientMessage(
                ModText.PERIPHERAL_PROXY_BLOCK_REMOVED.text,
                true,
            )
        }
        return InteractionResultHolder.consume(stack)
    }
}
