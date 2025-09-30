package site.siredvin.peripheralworks.subsystem.configurator

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
import site.siredvin.peripheralworks.data.ModTooltip

object NetworkManagerMode : ConfigurationMode {
    @Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
    override val modeID: ResourceLocation = ResourceLocation(PeripheralWorksCore.MOD_ID, "network_manager")
    override val description: Component = ModTooltip.NETWORK_MANAGER_MODE.text

    override fun onBlockClick(configurationTarget: BlockPos, stack: ItemStack, player: Player, hit: BlockHitResult, level: Level): InteractionResultHolder<ItemStack> {
        if (level.isClientSide || level !is ServerLevel) {
            return InteractionResultHolder.consume(stack)
        }
        val offhandItem = player.getItemInHand(InteractionHand.OFF_HAND)
        if (offhandItem.`is`(Items.NAME_TAG) && offhandItem.hoverName != offhandItem.item.getName(offhandItem)) {
            val name = offhandItem.hoverName.string
            val be = level.getBlockEntity(configurationTarget) as? NetworkManagerBlockEntity ?: return InteractionResultHolder.fail(stack)
            val peripheralRecord = be.peripherals.entries.firstOrNull { it.value == hit.blockPos } ?: return InteractionResultHolder.fail(stack)
            be.toggleGroup(name, peripheralRecord.key)
            return InteractionResultHolder.success(stack)
        }
        return InteractionResultHolder.consume(stack)
    }
}
