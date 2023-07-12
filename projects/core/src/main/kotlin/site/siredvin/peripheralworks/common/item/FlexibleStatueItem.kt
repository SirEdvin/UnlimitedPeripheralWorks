package site.siredvin.peripheralworks.common.item

import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import site.siredvin.peripheralium.common.blocks.BaseNBTBlock
import site.siredvin.peripheralium.common.items.DescriptiveBlockItem
import site.siredvin.peripheralworks.common.blockentity.FlexibleStatueBlockEntity
import site.siredvin.peripheralworks.data.ModTooltip

class FlexibleStatueItem(block: Block) : DescriptiveBlockItem(block, Properties()) {
    private fun extractName(stack: ItemStack): String? {
        val internalData = stack.getTagElement(BaseNBTBlock.INTERNAL_DATA_TAG) ?: return null
        if (!internalData.contains(FlexibleStatueBlockEntity.NAME_TAG)) return null
        return internalData.getString(FlexibleStatueBlockEntity.NAME_TAG)
    }

    private fun extractAuthor(stack: ItemStack): String? {
        val internalData = stack.getTagElement(BaseNBTBlock.INTERNAL_DATA_TAG) ?: return null
        if (!internalData.contains(FlexibleStatueBlockEntity.AUTHOR_TAG)) return null
        return internalData.getString(FlexibleStatueBlockEntity.AUTHOR_TAG)
    }

    override fun getName(stack: ItemStack): Component {
        val name = extractName(stack) ?: return super.getName(stack)
        return Component.literal(name)
    }

    override fun appendHoverText(
        itemStack: ItemStack,
        level: Level?,
        list: MutableList<Component>,
        tooltipFlag: TooltipFlag,
    ) {
        super.appendHoverText(itemStack, level, list, tooltipFlag)
        val author = extractAuthor(itemStack)
        if (author != null) {
            list.add(ModTooltip.FLEXIBLE_STATUE_AUTHOR.format(author))
        }
    }
}
