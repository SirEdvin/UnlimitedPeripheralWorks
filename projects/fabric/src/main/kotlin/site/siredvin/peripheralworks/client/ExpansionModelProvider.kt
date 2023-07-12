package site.siredvin.peripheralworks.client

import net.fabricmc.fabric.api.client.model.ModelProviderContext
import net.fabricmc.fabric.api.client.model.ModelResourceProvider
import net.minecraft.client.resources.model.UnbakedModel
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.common.block.FlexibleRealityAnchor
import site.siredvin.peripheralworks.common.block.FlexibleStatue

object ExpansionModelProvider : ModelResourceProvider {

    override fun loadModelResource(resourceId: ResourceLocation, context: ModelProviderContext): UnbakedModel? {
        if (resourceId == FlexibleRealityAnchor.BLOCK_MODEL_ID || resourceId == FlexibleRealityAnchor.ITEM_MODEL_ID) {
            return FlexibleRealityAnchorUnbakedModel
        } else if (resourceId == FlexibleStatue.BLOCK_MODEL_ID || resourceId == FlexibleStatue.ITEM_MODEL_ID) {
            return FlexibleStatueUnbakedModel
        }
        return null
    }
}
