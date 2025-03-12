package site.siredvin.peripheralworks.common.item

import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.block.Block
import site.siredvin.broccolium.modules.base.item.DescriptiveBlockItem
import site.siredvin.peripheralworks.common.blockentity.FlexibleStatueBlockEntity
import site.siredvin.peripheralworks.data.ModTooltip

class FlexibleStatueItem(block: Block) : DescriptiveBlockItem(block, Properties()) {
    private fun extractName(stack: ItemStack): String? {
        val internalData = stack.get(DataComponents.CUSTOM_DATA)?.copyTag() ?: return null
        if (!internalData.contains(FlexibleStatueBlockEntity.NAME_TAG)) return null
        return internalData.getString(FlexibleStatueBlockEntity.NAME_TAG)
    }

    private fun extractAuthor(stack: ItemStack): String? {
        val internalData = stack.get(DataComponents.CUSTOM_DATA)?.copyTag() ?: return null
        if (!internalData.contains(FlexibleStatueBlockEntity.AUTHOR_TAG)) return null
        return internalData.getString(FlexibleStatueBlockEntity.AUTHOR_TAG)
    }

    override fun getName(stack: ItemStack): Component {
        val name = extractName(stack) ?: return super.getName(stack)
        return Component.literal(name)
    }

    override fun appendHoverText(
        itemStack: ItemStack,
        context: TooltipContext,
        list: MutableList<Component>,
        tooltipFlag: TooltipFlag,
    ) {
        super.appendHoverText(itemStack, context, list, tooltipFlag)
        val author = extractAuthor(itemStack)
        if (author != null) {
            list.add(ModTooltip.FLEXIBLE_STATUE_AUTHOR.format(author))
        }
    }
}
