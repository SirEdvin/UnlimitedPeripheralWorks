package site.siredvin.peripheralworks.subsystem.configurator

import net.minecraft.core.BlockPos
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
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
    enum class VisualizationMode { ALL, SELECTED, SELECTED_AND_UNGROUPED, UNGROUPED }

    private const val SELECTED_GROUP = "selectedNetworkGroup"
    private const val EXPANDED_GROUP_PATHS = "expandedNetworkGroupPaths"
    private const val VISUALIZATION_MODE = "networkVisualizationMode"
    private const val MAX_EXPANDED_GROUP_PATHS = 256

    @Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
    override val modeID: ResourceLocation = ResourceLocation(PeripheralWorksCore.MOD_ID, "network_manager")
    override val description: Component = ModTooltip.NETWORK_MANAGER_MODE.text

    override fun onBlockClick(configurationTarget: BlockPos, stack: ItemStack, player: Player, hit: BlockHitResult, level: Level): InteractionResultHolder<ItemStack> {
        if (!UltimateConfigurator.isActiveModeDimension(stack, level)) return InteractionResultHolder.fail(stack)
        if (level.isClientSide || level !is ServerLevel) {
            return InteractionResultHolder.consume(stack)
        }
        val name = getSelectedGroup(stack)
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

    fun getSelectedGroup(stack: ItemStack): String? = stack.tag?.getString(SELECTED_GROUP)?.takeIf(String::isNotEmpty)

    fun setSelectedGroup(stack: ItemStack, group: String) {
        stack.orCreateTag.putString(SELECTED_GROUP, group)
    }

    fun clearSelectedGroup(stack: ItemStack) {
        stack.tag?.remove(SELECTED_GROUP)
    }

    fun getVisualizationMode(stack: ItemStack): VisualizationMode = stack.tag?.getString(VISUALIZATION_MODE)
        ?.let { value -> VisualizationMode.entries.firstOrNull { it.name == value } }
        ?: VisualizationMode.ALL

    fun setVisualizationMode(stack: ItemStack, mode: VisualizationMode) {
        stack.orCreateTag.putString(VISUALIZATION_MODE, mode.name)
    }

    fun getExpandedGroupPaths(stack: ItemStack): Set<String> {
        val paths = stack.tag?.getList(EXPANDED_GROUP_PATHS, Tag.TAG_STRING.toInt()) ?: return emptySet()
        return paths.mapTo(mutableSetOf()) { it.asString }
    }

    fun setGroupExpanded(stack: ItemStack, path: String, expanded: Boolean) {
        val paths = getExpandedGroupPaths(stack).toMutableSet()
        if (expanded) {
            if (paths.size >= MAX_EXPANDED_GROUP_PATHS) return
            paths.add(path)
        } else {
            paths.remove(path)
        }
        if (paths.isEmpty()) {
            stack.tag?.remove(EXPANDED_GROUP_PATHS)
            return
        }
        stack.orCreateTag.put(
            EXPANDED_GROUP_PATHS,
            ListTag().apply { paths.sorted().forEach { add(StringTag.valueOf(it)) } },
        )
    }

    override fun extraTooltips(itemStack: ItemStack, tooltip: MutableList<Component>) {
        tooltip.add(ModTooltip.NETWORK_MANAGER_SELECTED_GROUP.format(getSelectedGroup(itemStack) ?: "-"))
    }

    override fun clearData(itemStack: ItemStack) {
        itemStack.tag?.remove(SELECTED_GROUP)
        itemStack.tag?.remove(EXPANDED_GROUP_PATHS)
        itemStack.tag?.remove(VISUALIZATION_MODE)
    }
}
