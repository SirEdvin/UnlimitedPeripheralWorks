package site.siredvin.peripheralworks.subsystem.configurator

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
import site.siredvin.peripheralworks.common.item.UltimateConfigurator
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.peripheralworks.data.ModTooltip
import site.siredvin.peripheralworks.xplat.ModClientPlatform

object NetworkManagerMode : ConfigurationMode {
    @Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
    override val modeID: ResourceLocation = ResourceLocation(PeripheralWorksCore.MOD_ID, "network_manager")
    override val description: Component = ModTooltip.NETWORK_MANAGER_MODE.text

    override fun onBlockClick(configurationTarget: BlockPos, stack: ItemStack, player: Player, hit: BlockHitResult, level: Level): InteractionResultHolder<ItemStack> {
        if (!UltimateConfigurator.isActiveModeDimension(stack, level)) return InteractionResultHolder.fail(stack)
        if (level.isClientSide || level !is ServerLevel) {
            return InteractionResultHolder.consume(stack)
        }
        val name = UltimateConfigurator.getSelectedNetworkGroup(stack)
        if (name == null) {
            player.displayClientMessage(ModText.NETWORK_MANAGER_GROUP_SELECT_REQUIRED.text, true)
            return InteractionResultHolder.fail(stack)
        }
        val be = level.getBlockEntity(configurationTarget) as? NetworkManagerBlockEntity
        val peripheralRecord = be?.peripherals?.entries?.firstOrNull { it.value == hit.blockPos }
        val result = if (peripheralRecord == null) null else be.toggleGroup(name, peripheralRecord.key)
        val message = when (result) {
            NetworkManagerBlockEntity.GroupOperationResult.SUCCESS -> ModText.NETWORK_MANAGER_GROUP_MEMBERSHIP_TOGGLED
            NetworkManagerBlockEntity.GroupOperationResult.GROUP_MISSING -> ModText.NETWORK_MANAGER_GROUP_STALE
            else -> ModText.NETWORK_MANAGER_PERIPHERAL_MISSING
        }
        player.displayClientMessage(message.text, true)
        return if (result == NetworkManagerBlockEntity.GroupOperationResult.SUCCESS) InteractionResultHolder.success(stack) else InteractionResultHolder.fail(stack)
    }

    override fun onBlockMiss(configurationTarget: BlockPos, stack: ItemStack, player: Player, level: Level): InteractionResultHolder<ItemStack> {
        if (!UltimateConfigurator.isActiveModeDimension(stack, level)) return InteractionResultHolder.fail(stack)
        if (level.isClientSide) ModClientPlatform.openNetworkManagerScreen(configurationTarget)
        return InteractionResultHolder.consume(stack)
    }
}
