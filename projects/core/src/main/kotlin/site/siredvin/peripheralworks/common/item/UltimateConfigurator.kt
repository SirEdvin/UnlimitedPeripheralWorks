package site.siredvin.peripheralworks.common.item

import net.minecraft.core.BlockPos
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtUtils
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
import site.siredvin.peripheralworks.subsystem.configurator.ConfiguratorTarget
import site.siredvin.peripheralworks.xplat.ModClientPlatform

class UltimateConfigurator : DescriptiveItem(Properties().stacksTo(1)) {

    companion object {
        const val ACTIVE_MOD_NAME = "activeMod"
        const val ACTIVE_MOD_POS = "activeModPos"
        const val ACTIVE_MOD_DIMENSION = "activeModDimension"
        const val RECENT_TARGETS = "recentTargets"
        const val FAVORITE_TARGETS = "favoriteTargets"
        const val MAX_RECENT_TARGETS = 3
        const val MAX_FAVORITE_TARGETS = 16
        const val MAX_TARGET_HISTORY = MAX_RECENT_TARGETS + MAX_FAVORITE_TARGETS
        const val FAVORITE_TEXT_COLOR = "favoriteTextColor"
        const val FAVORITE_BOX_COLOR = "favoriteBoxColor"
        const val DEFAULT_FAVORITE_TEXT_COLOR = 0xffffff
        const val DEFAULT_FAVORITE_BOX_COLOR = 0xffaa00
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
        val modeID = ResourceLocation.tryParse(data.getString(ACTIVE_MOD_NAME)) ?: return null
        val configurationMode = ConfiguratorModeRegistry.get(modeID) ?: return null
        return Pair(
            configurationMode,
            NbtUtils.readBlockPos(data.getCompound(ACTIVE_MOD_POS)),
        )
    }

    fun getFavoriteTargets(stack: ItemStack): List<ConfiguratorTarget> = readTargets(stack, FAVORITE_TARGETS, MAX_FAVORITE_TARGETS)

    private fun getStoredHistory(stack: ItemStack): List<ConfiguratorTarget> = readTargets(stack, RECENT_TARGETS, MAX_TARGET_HISTORY)

    fun getTargetHistory(stack: ItemStack): List<ConfiguratorTarget> {
        val favorites = getFavoriteTargets(stack)
        val history = getStoredHistory(stack)
        var nonFavorites = 0
        return (history + favorites.filter { favorite -> history.none(favorite::matches) }).mapNotNull { target ->
            favorites.firstOrNull(target::matches) ?: target.takeIf { nonFavorites++ < MAX_RECENT_TARGETS }
        }
    }

    fun getRecentTargets(stack: ItemStack): List<ConfiguratorTarget> {
        val favorites = getFavoriteTargets(stack)
        return getStoredHistory(stack).filter { target -> favorites.none(target::matches) }.take(MAX_RECENT_TARGETS)
    }

    private fun readTargets(stack: ItemStack, key: String, limit: Int): List<ConfiguratorTarget> {
        val list = stack.tag?.getList(key, Tag.TAG_COMPOUND.toInt()) ?: return emptyList()
        return buildList {
            for (index in 0 until list.size) {
                val target = ConfiguratorTarget.fromNBT(list.getCompound(index)) ?: continue
                if (none(target::matches)) add(target)
                if (size == limit) break
            }
        }
    }

    private fun writeTargets(stack: ItemStack, key: String, targets: List<ConfiguratorTarget>, limit: Int) {
        val list = ListTag()
        targets.distinctBy { it.dimensionID to it.pos }.take(limit).forEach { list.add(it.toNBT()) }
        stack.orCreateTag.put(key, list)
    }

    private fun writeHistory(stack: ItemStack, targets: List<ConfiguratorTarget>, favorites: List<ConfiguratorTarget>) {
        var nonFavorites = 0
        val complete = targets + favorites.filter { favorite -> targets.none(favorite::matches) }
        writeTargets(
            stack,
            RECENT_TARGETS,
            complete.filter { target -> favorites.any(target::matches) || nonFavorites++ < MAX_RECENT_TARGETS },
            MAX_TARGET_HISTORY,
        )
    }

    fun saveActiveMode(stack: ItemStack, mode: ConfigurationMode, targetBlock: BlockPos, level: Level) {
        getActiveMode(stack)?.first?.clearData(stack)
        val data = stack.orCreateTag
        data.putString(ACTIVE_MOD_NAME, mode.modeID.toString())
        data.put(ACTIVE_MOD_POS, NbtUtils.writeBlockPos(targetBlock))
        data.putString(ACTIVE_MOD_DIMENSION, level.dimension().location().toString())
        val target = ConfiguratorTarget(mode.modeID, level.dimension().location(), targetBlock)
        val favorites = getFavoriteTargets(stack).map { if (it.matches(target)) it.copy(modeID = target.modeID) else it }
        writeTargets(stack, FAVORITE_TARGETS, favorites, MAX_FAVORITE_TARGETS)
        writeHistory(stack, listOf(target) + getStoredHistory(stack).filterNot(target::matches), favorites)
    }

    fun clearActiveMode(stack: ItemStack): ItemStack {
        val data = stack.tag ?: return stack
        getActiveMode(stack)?.first?.clearData(stack)
        data.remove(ACTIVE_MOD_NAME)
        data.remove(ACTIVE_MOD_POS)
        data.remove(ACTIVE_MOD_DIMENSION)
        return stack
    }

    fun toggleFavorite(stack: ItemStack, target: ConfiguratorTarget): FavoriteResult {
        val stored = getTargetHistory(stack).firstOrNull(target::matches) ?: return FavoriteResult.REJECTED
        val favorites = getFavoriteTargets(stack)
        if (favorites.any(target::matches)) {
            val updated = favorites.filterNot(target::matches)
            writeTargets(stack, FAVORITE_TARGETS, updated, MAX_FAVORITE_TARGETS)
            writeHistory(stack, getStoredHistory(stack), updated)
            return FavoriteResult.REMOVED
        }
        if (favorites.size >= MAX_FAVORITE_TARGETS) return FavoriteResult.LIMIT
        val updated = listOf(stored.copy(name = null)) + favorites
        writeTargets(stack, FAVORITE_TARGETS, updated, MAX_FAVORITE_TARGETS)
        writeHistory(stack, getStoredHistory(stack), updated)
        return FavoriteResult.ADDED
    }

    fun renameFavorite(stack: ItemStack, target: ConfiguratorTarget, name: String): Boolean {
        if (name.length > ConfiguratorTarget.MAX_NAME_LENGTH) return false
        val favorites = getFavoriteTargets(stack)
        if (favorites.none(target::matches)) return false
        writeTargets(stack, FAVORITE_TARGETS, favorites.map { if (it.matches(target)) it.copy(name = name.ifEmpty { null }) else it }, MAX_FAVORITE_TARGETS)
        return true
    }

    fun setFavoriteColor(stack: ItemStack, target: ConfiguratorTarget, color: Int, text: Boolean): Boolean {
        if (color !in 0..0xffffff) return false
        val favorites = getFavoriteTargets(stack)
        if (favorites.none(target::matches)) return false
        writeTargets(stack, FAVORITE_TARGETS, favorites.map { if (it.matches(target)) if (text) it.copy(textColor = color) else it.copy(boxColor = color) else it }, MAX_FAVORITE_TARGETS)
        return true
    }

    fun getFavoriteTextColor(stack: ItemStack): Int = stack.tag?.takeIf { it.contains(FAVORITE_TEXT_COLOR, Tag.TAG_INT.toInt()) }?.getInt(FAVORITE_TEXT_COLOR)?.takeIf { it in 0..0xffffff } ?: DEFAULT_FAVORITE_TEXT_COLOR

    fun getFavoriteBoxColor(stack: ItemStack): Int = stack.tag?.takeIf { it.contains(FAVORITE_BOX_COLOR, Tag.TAG_INT.toInt()) }?.getInt(FAVORITE_BOX_COLOR)?.takeIf { it in 0..0xffffff } ?: DEFAULT_FAVORITE_BOX_COLOR

    fun setSettings(stack: ItemStack, name: String?, textColor: Int? = null, boxColor: Int? = null): Boolean {
        if (name != null) {
            if (name.length > ConfiguratorTarget.MAX_NAME_LENGTH) return false
            if (name.isEmpty()) stack.resetHoverName() else stack.hoverName = Component.literal(name)
        }
        if (textColor != null) {
            if (textColor !in 0..0xffffff) return false
            stack.orCreateTag.putInt(FAVORITE_TEXT_COLOR, textColor)
        }
        if (boxColor != null) {
            if (boxColor !in 0..0xffffff) return false
            stack.orCreateTag.putInt(FAVORITE_BOX_COLOR, boxColor)
        }
        return true
    }

    fun selectTarget(stack: ItemStack, level: Level, target: ConfiguratorTarget): SelectionResult {
        val stored = getTargetHistory(stack).firstOrNull(target::matches) ?: return SelectionResult.REJECTED
        if (stored.dimensionID != level.dimension().location() || !level.isLoaded(stored.pos)) return SelectionResult.UNAVAILABLE
        val mode = ConfiguratorModeRegistry.get(level.getBlockState(stored.pos))
        if (mode?.modeID != stored.modeID) return SelectionResult.UNAVAILABLE
        saveActiveMode(stack, mode, stored.pos, level)
        return SelectionResult.SUCCESS
    }

    enum class FavoriteResult { ADDED, REMOVED, LIMIT, REJECTED }
    enum class SelectionResult { SUCCESS, UNAVAILABLE, REJECTED }

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
                val activeModePair = getActiveMode(itemStack)
                if (activeModePair == null) {
                    if (level.isClientSide) ModClientPlatform.openConfiguratorTargetHistoryScreen()
                    return InteractionResultHolder.consume(itemStack)
                }
                return activeModePair.first.onBlockMiss(activeModePair.second, itemStack, player, level)
            }
        } else {
            val activeModePair = getActiveMode(itemStack) ?: return tryActivateMode(itemStack, player, blockHitResult, level)
            return activeModePair.first.onBlockClick(activeModePair.second, itemStack, player, blockHitResult, level)
        }
    }

    override fun isFoil(itemStack: ItemStack): Boolean = getActiveMode(itemStack) != null
}
