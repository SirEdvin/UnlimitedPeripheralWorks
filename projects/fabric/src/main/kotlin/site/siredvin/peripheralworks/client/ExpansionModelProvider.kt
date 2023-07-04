package site.siredvin.peripheralworks.client

import net.fabricmc.fabric.api.client.model.ModelProviderContext
import net.fabricmc.fabric.api.client.model.ModelResourceProvider
import net.minecraft.client.resources.model.UnbakedModel
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.common.block.FlexibleRealityAnchor

object ExpansionModelProvider : ModelResourceProvider {

    override fun loadModelResource(resourceId: ResourceLocation, context: ModelProviderContext): UnbakedModel? {
        if (resourceId == FlexibleRealityAnchor.EXTRA_MODEL_ID) {
            return FlexibleRealityAnchorModel()
        }
        return null
    }
}
