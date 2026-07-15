package site.siredvin.peripheralworks.subsystem.configurator

import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.peripheralworks.data.ModTooltip

object NetworkManagerMode : ConfigurationMode {
    override val modeID: ResourceLocation = ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "network_manager")
    override val description: Component = ModTooltip.NETWORK_MANAGER_MODE.text

    const val RANGE_TAG = "networkManagerRange"
    private const val DEFAULT_RANGE = 32
    private val appropriateRanges = listOf(64, 32, 16, 8, 4)

    fun getRange(stack: ItemStack): Int {
        val data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
        if (!data.contains(RANGE_TAG)) {
            data.putInt(RANGE_TAG, DEFAULT_RANGE)
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data))
        }
        return data.getInt(RANGE_TAG)
    }

    override fun extraTooltips(itemStack: ItemStack, tooltip: MutableList<Component>) {
        tooltip.add(ModTooltip.NETWORK_MANAGER_CURRENT_RANGE.format(getRange(itemStack)))
    }

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

    override fun onSwing(
        configurationTarget: BlockPos,
        stack: ItemStack,
        owner: Player,
    ): Boolean {
        val currentRange = getRange(stack)
        val index = (appropriateRanges.indexOf(currentRange) + 1) % appropriateRanges.size
        val data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
        data.putInt(RANGE_TAG, appropriateRanges[index])
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data))
        if (owner is ServerPlayer) {
            owner.displayClientMessage(ModText.NETWORK_MANAGER_MOD_RADIUS_CHANGE.format(appropriateRanges[index]), true)
        }
        return false
    }
}
