package site.siredvin.peripheralworks.integrations.kubejs

import dev.latvian.mods.kubejs.block.entity.BlockEntityInfo
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import site.siredvin.peripheralworks.common.block.CustomPedestal

class ItemPedestalBuilder(i: ResourceLocation) : AbstractPedestalBuilder(i) {

    override fun createObject(): Block = CustomPedestal(createProperties(), blockEntityInfo::createBlockEntity)

    override fun buildBlockEntityInfo(): BlockEntityInfo = ItemPedestalBlockEntityInfo(this)
}
