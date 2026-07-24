package site.siredvin.peripheralworks.common.item

import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.NbtUtils
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import site.siredvin.broccolium.modules.base.item.DescriptiveItem
import site.siredvin.peripheralworks.data.ModTooltip
import site.siredvin.peripheralworks.subsystem.configurator.ConfigurationMode
import site.siredvin.peripheralworks.subsystem.configurator.ConfiguratorModeRegistry

class UltimateConfigurator : DescriptiveItem(Properties().stacksTo(1)) {

    companion object {
        const val ACTIVE_MOD_NAME = "activeMod"
        const val ACTIVE_MOD_POS = "activeModPos"
        const val ACTIVE_MOD_DIMENSION = "activeModDimension"
        fun isActiveModeDimension(stack: ItemStack, level: Level): Boolean = stack.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString(ACTIVE_MOD_DIMENSION) == level.dimension().location().toString()
    }

    override fun appendHoverText(
        itemStack: ItemStack,
        context: TooltipContext,
        list: MutableList<Component>,
        tooltipFlag: TooltipFlag,
    ) {
        super.appendHoverText(itemStack, context, list, tooltipFlag)
        val activeMode = getActiveMode(itemStack)
        if (activeMode != null) {
            list.add(ModTooltip.ACTIVE_CONFIGURATION_MODE.text)
            list.add(activeMode.first.description)
            list.add(ModTooltip.CONFIGURATION_TARGET_BLOCK.format(activeMode.second.toString()))
            activeMode.first.extraTooltips(itemStack, list)
        }
    }

    fun getActiveMode(stack: ItemStack): Pair<ConfigurationMode, BlockPos>? {
        val data = stack.get(DataComponents.CUSTOM_DATA)?.copyTag() ?: return null
        if (!data.contains(ACTIVE_MOD_NAME)) {
            return null
        }
        if (!data.contains(ACTIVE_MOD_POS)) {
            return null
        }
        val configurationMode = ConfiguratorModeRegistry.get(ResourceLocation.parse(data.getString(ACTIVE_MOD_NAME))) ?: return null
        return Pair(
            configurationMode,
            NbtUtils.readBlockPos(data, ACTIVE_MOD_POS).get(),
        )
    }

    private fun saveActiveMode(stack: ItemStack, mode: ConfigurationMode, targetBlock: BlockPos, level: Level) {
        getActiveMode(stack)?.first?.clearData(stack)
        val data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
        data.putString(ACTIVE_MOD_NAME, mode.modeID.toString())
        data.put(ACTIVE_MOD_POS, NbtUtils.writeBlockPos(targetBlock))
        data.putString(ACTIVE_MOD_DIMENSION, level.dimension().location().toString())
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data))
    }

    private fun clearActiveMode(stack: ItemStack): ItemStack {
        getActiveMode(stack)?.first?.clearData(stack)
        val data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
        data.remove(ACTIVE_MOD_NAME)
        data.remove(ACTIVE_MOD_POS)
        data.remove(ACTIVE_MOD_DIMENSION)
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data))
        return stack
    }

    private fun tryActivateMode(stack: ItemStack, player: Player, hit: BlockHitResult, level: Level): InteractionResultHolder<ItemStack> {
        if (player.pose == Pose.CROUCHING) {
            val targetState = level.getBlockState(hit.blockPos)
            val possibleMode = ConfiguratorModeRegistry.get(targetState)
            if (possibleMode != null) {
                saveActiveMode(stack, possibleMode, hit.blockPos, level)
                return InteractionResultHolder.consume(stack)
            }
        }
        return InteractionResultHolder.pass(stack)
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
                val activeModePair = getActiveMode(itemStack) ?: return InteractionResultHolder.pass(itemStack)
                return activeModePair.first.onBlockMiss(activeModePair.second, itemStack, player, level)
            }
        } else {
            val activeModePair = getActiveMode(itemStack) ?: return tryActivateMode(itemStack, player, blockHitResult, level)
            return activeModePair.first.onBlockClick(activeModePair.second, itemStack, player, blockHitResult, level)
        }
    }

    override fun isFoil(itemStack: ItemStack): Boolean = getActiveMode(itemStack) != null
}
