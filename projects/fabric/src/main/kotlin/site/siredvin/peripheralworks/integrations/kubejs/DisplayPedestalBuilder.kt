package site.siredvin.peripheralworks.integrations.kubejs

import dev.latvian.mods.kubejs.block.entity.BlockEntityInfo
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import site.siredvin.peripheralworks.common.block.CustomDisplayPedestal

class DisplayPedestalBuilder(i: ResourceLocation) : AbstractPedestalBuilder(i) {

    override fun createObject(): Block = CustomDisplayPedestal(createProperties(), blockEntityInfo::createBlockEntity)

    override fun buildBlockEntityInfo(): BlockEntityInfo = DisplayPedestalBlockEntityInfo(this)
}
