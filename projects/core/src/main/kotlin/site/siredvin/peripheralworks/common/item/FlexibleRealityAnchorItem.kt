package site.siredvin.peripheralworks.common.item

import net.minecraft.nbt.NbtUtils
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralium.common.blocks.BaseNBTBlock
import site.siredvin.peripheralium.common.items.DescriptiveBlockItem
import site.siredvin.peripheralium.xplat.XplatRegistries
import site.siredvin.peripheralworks.common.blockentity.FlexibleRealityAnchorTileEntity
import site.siredvin.peripheralworks.data.ModText

class FlexibleRealityAnchorItem(block: Block) : DescriptiveBlockItem(block, Properties()) {

    fun extractMimicBlock(stack: ItemStack): BlockState? {
        val internalData = stack.getTagElement(BaseNBTBlock.INTERNAL_DATA_TAG) ?: return null
        val mimicTag = internalData.getCompound(FlexibleRealityAnchorTileEntity.MIMIC_TAG)
        if (mimicTag.isEmpty) return null
        val mimicState = NbtUtils.readBlockState(XplatRegistries.BLOCKS, mimicTag)
        if (mimicState.isAir) return null
        return mimicState
    }

    override fun getName(stack: ItemStack): Component {
        val mimicState = extractMimicBlock(stack) ?: return super.getName(stack)
        val hoverName = mimicState.block.asItem().defaultInstance.hoverName
        return ModText.DEFINITELY_NOT.text.append(hoverName)
    }
}
