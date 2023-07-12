package site.siredvin.peripheralworks.client

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.*
import net.minecraft.resources.ResourceLocation
import java.util.function.Function

@Environment(EnvType.CLIENT)
object FlexibleRealityAnchorUnbakedModel : UnbakedModel {
    override fun getDependencies(): MutableCollection<ResourceLocation> {
        return mutableListOf()
    }

    override fun resolveParents(function: Function<ResourceLocation, UnbakedModel>) {
    }

    override fun bake(
        modelBaker: ModelBaker,
        function: Function<Material, TextureAtlasSprite>,
        modelState: ModelState,
        resourceLocation: ResourceLocation,
    ): BakedModel {
        return FlexibleRealityAnchorModel
    }
}
