package site.siredvin.peripheralworks.common.item

import net.minecraft.nbt.NbtUtils
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.broccolium.modules.base.block.BaseNBTBlock
import site.siredvin.broccolium.modules.base.item.DescriptiveBlockItem
import site.siredvin.broccolium.modules.platform.PlatformRegistries
import site.siredvin.peripheralworks.common.blockentity.FlexibleRealityAnchorBlockEntity
import site.siredvin.peripheralworks.data.ModText

class FlexibleRealityAnchorItem(block: Block) : DescriptiveBlockItem(block, Properties()) {

    fun extractMimicBlock(stack: ItemStack): BlockState? {
        val internalData = stack.getTagElement(BaseNBTBlock.INTERNAL_DATA_TAG) ?: return null
        val mimicTag = internalData.getCompound(FlexibleRealityAnchorBlockEntity.MIMIC_TAG)
        if (mimicTag.isEmpty) return null
        val mimicState = NbtUtils.readBlockState(PlatformRegistries.BLOCKS, mimicTag)
        if (mimicState.isAir) return null
        return mimicState
    }

    override fun getName(stack: ItemStack): Component {
        val mimicState = extractMimicBlock(stack) ?: return super.getName(stack)
        val hoverName = mimicState.block.asItem().defaultInstance.hoverName
        return ModText.DEFINITELY_NOT.text.append(hoverName)
    }
}
