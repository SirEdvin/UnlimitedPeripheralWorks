package site.siredvin.peripheralworks.common.item

import net.minecraft.core.BlockPos
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
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
import site.siredvin.broccolium.modules.base.item.DescriptiveItem
import site.siredvin.peripheralworks.data.ModTooltip
import site.siredvin.peripheralworks.subsystem.configurator.ConfigurationMode
import site.siredvin.peripheralworks.subsystem.configurator.ConfiguratorModeRegistry

class UltimateConfigurator : DescriptiveItem(Properties().stacksTo(1)) {

    companion object {
        const val ACTIVE_MOD_NAME = "activeMod"
        const val ACTIVE_MOD_POS = "activeModPos"
        const val ACTIVE_MOD_DIMENSION = "activeModDimension"
        const val SELECTED_NETWORK_GROUP = "selectedNetworkGroup"
        const val EXPANDED_NETWORK_GROUP_PATHS = "expandedNetworkGroupPaths"
        const val MAX_EXPANDED_NETWORK_GROUP_PATHS = 256

        fun getSelectedNetworkGroup(stack: ItemStack): String? = stack.tag?.getString(SELECTED_NETWORK_GROUP)?.takeIf(String::isNotEmpty)

        fun setSelectedNetworkGroup(stack: ItemStack, group: String) {
            stack.orCreateTag.putString(SELECTED_NETWORK_GROUP, group)
        }

        fun clearSelectedNetworkGroup(stack: ItemStack) {
            stack.tag?.remove(SELECTED_NETWORK_GROUP)
        }

        fun getExpandedNetworkGroupPaths(stack: ItemStack): Set<String> {
            val paths = stack.tag?.getList(EXPANDED_NETWORK_GROUP_PATHS, Tag.TAG_STRING.toInt()) ?: return emptySet()
            return paths.mapTo(mutableSetOf()) { it.asString }
        }

        fun setNetworkGroupExpanded(stack: ItemStack, path: String, expanded: Boolean) {
            val paths = getExpandedNetworkGroupPaths(stack).toMutableSet()
            if (expanded) {
                if (paths.size >= MAX_EXPANDED_NETWORK_GROUP_PATHS) return
                paths.add(path)
            } else {
                paths.remove(path)
            }
            if (paths.isEmpty()) {
                stack.tag?.remove(EXPANDED_NETWORK_GROUP_PATHS)
                return
            }
            stack.orCreateTag.put(
                EXPANDED_NETWORK_GROUP_PATHS,
                ListTag().apply { paths.sorted().forEach { add(StringTag.valueOf(it)) } },
            )
        }

        fun clearExpandedNetworkGroupPaths(stack: ItemStack) {
            stack.tag?.remove(EXPANDED_NETWORK_GROUP_PATHS)
        }

        fun isActiveModeDimension(stack: ItemStack, level: Level): Boolean = stack.tag?.getString(ACTIVE_MOD_DIMENSION) == level.dimension().location().toString()
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
            list.add(ModTooltip.ACTIVE_CONFIGURATION_MODE.text)
            list.add(activeMode.first.description)
            list.add(ModTooltip.CONFIGURATION_TARGET_BLOCK.format(activeMode.second.toString()))
            activeMode.first.extraTooltips(itemStack, list)
            if (activeMode.first.modeID.namespace == "peripheralworks" && activeMode.first.modeID.path == "network_manager") {
                list.add(ModTooltip.NETWORK_MANAGER_SELECTED_GROUP.format(getSelectedNetworkGroup(itemStack) ?: "-"))
            }
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
        @Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
        val configurationMode = ConfiguratorModeRegistry.get(ResourceLocation(data.getString(ACTIVE_MOD_NAME))) ?: return null
        return Pair(
            configurationMode,
            NbtUtils.readBlockPos(data.getCompound(ACTIVE_MOD_POS)),
        )
    }

    private fun saveActiveMode(stack: ItemStack, mode: ConfigurationMode, targetBlock: BlockPos, level: Level) {
        clearSelectedNetworkGroup(stack)
        clearExpandedNetworkGroupPaths(stack)
        val data = stack.orCreateTag
        data.putString(ACTIVE_MOD_NAME, mode.modeID.toString())
        data.put(ACTIVE_MOD_POS, NbtUtils.writeBlockPos(targetBlock))
        data.putString(ACTIVE_MOD_DIMENSION, level.dimension().location().toString())
    }

    private fun clearActiveMode(stack: ItemStack): ItemStack {
        val data = stack.tag ?: return stack
        data.remove(ACTIVE_MOD_NAME)
        data.remove(ACTIVE_MOD_POS)
        data.remove(ACTIVE_MOD_DIMENSION)
        clearSelectedNetworkGroup(stack)
        clearExpandedNetworkGroupPaths(stack)
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
