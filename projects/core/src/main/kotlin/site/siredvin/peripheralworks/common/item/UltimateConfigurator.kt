package site.siredvin.peripheralworks.common.item

import net.minecraft.core.BlockPos
import net.minecraft.nbt.NbtUtils
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import site.siredvin.peripheralium.common.items.DescriptiveItem
import site.siredvin.peripheralium.util.text
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.toolkit.configurator.ConfigurationMode
import site.siredvin.peripheralworks.toolkit.configurator.ConfiguratorModeRegistry

class UltimateConfigurator : DescriptiveItem(Properties().stacksTo(1)) {

    companion object {
        const val ACTIVE_MOD_NAME = "activeMod"
        const val ACTIVE_MOD_POS = "activeModPos"
    }

    override fun appendHoverText(
        itemStack: ItemStack,
        level: Level?,
        list: MutableList<Component>,
        tooltipFlag: TooltipFlag,
    ) {
        super.appendHoverText(itemStack, level, list, tooltipFlag)
        val activeMode = getActiveMode(itemStack)
        if (activeMode != null) {
            list.add(text(PeripheralWorksCore.MOD_ID, "active_configuration_mode"))
            list.add(activeMode.first.description)
            list.add(text(PeripheralWorksCore.MOD_ID, "configuration_target_block", activeMode.second.toString()))
        }
    }

    fun getActiveMode(stack: ItemStack): Pair<ConfigurationMode, BlockPos>? {
        val data = stack.tag ?: return null
        if (!data.contains(ACTIVE_MOD_NAME)) {
            return null
        }
        if (!data.contains(ACTIVE_MOD_POS)) {
            return null
        }
        val configurationMode = ConfiguratorModeRegistry.get(ResourceLocation(data.getString(ACTIVE_MOD_NAME))) ?: return null
        return Pair(
            configurationMode,
            NbtUtils.readBlockPos(data.getCompound(ACTIVE_MOD_POS)),
        )
    }

    private fun saveActiveMode(stack: ItemStack, mode: ConfigurationMode, targetBlock: BlockPos) {
        val data = stack.orCreateTag
        data.putString(ACTIVE_MOD_NAME, mode.modeID.toString())
        data.put(ACTIVE_MOD_POS, NbtUtils.writeBlockPos(targetBlock))
    }

    private fun clearActiveMode(stack: ItemStack): ItemStack {
        val data = stack.tag ?: return stack
        data.remove(ACTIVE_MOD_NAME)
        data.remove(ACTIVE_MOD_POS)
        return stack
    }

    private fun tryActivateMode(stack: ItemStack, player: Player, hit: BlockHitResult, level: Level): InteractionResultHolder<ItemStack> {
        if (player.pose == Pose.CROUCHING) {
            val targetState = level.getBlockState(hit.blockPos)
            val possibleMode = ConfiguratorModeRegistry.get(targetState)
            if (possibleMode != null) {
                saveActiveMode(stack, possibleMode, hit.blockPos)
            }
        }
        return InteractionResultHolder.consume(stack)
    }

    override fun use(
        level: Level,
        player: Player,
        interactionHand: InteractionHand,
    ): InteractionResultHolder<ItemStack> {
        if (interactionHand == InteractionHand.OFF_HAND) {
            return super.use(level, player, interactionHand)
        }
        val itemStack = player.getItemInHand(interactionHand)
        val blockHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE)
        return if (blockHitResult.type == HitResult.Type.MISS) {
            if (player.pose == Pose.CROUCHING) {
                InteractionResultHolder.consume(clearActiveMode(itemStack))
            } else {
                InteractionResultHolder.pass(itemStack)
            }
        } else {
            val activeModePair = getActiveMode(itemStack) ?: return tryActivateMode(itemStack, player, blockHitResult, level)
            return activeModePair.first.onBlockClick(activeModePair.second, itemStack, player, blockHitResult, level)
        }
    }

    override fun isFoil(itemStack: ItemStack): Boolean {
        return getActiveMode(itemStack) != null
    }
}
