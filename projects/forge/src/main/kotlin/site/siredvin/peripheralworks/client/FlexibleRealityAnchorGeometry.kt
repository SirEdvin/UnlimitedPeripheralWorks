package site.siredvin.peripheralworks.client

import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.resources.model.Material
import net.minecraft.client.resources.model.ModelBaker
import net.minecraft.client.resources.model.ModelState
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.client.model.geometry.IGeometryBakingContext
import net.minecraftforge.client.model.geometry.IUnbakedGeometry
import java.util.function.Function

class FlexibleRealityAnchorGeometry : IUnbakedGeometry<FlexibleRealityAnchorGeometry> {
    override fun bake(
        context: IGeometryBakingContext,
        baker: ModelBaker,
        spriteGetter: Function<Material, TextureAtlasSprite>,
        modelState: ModelState,
        overrides: ItemOverrides,
        modelLocation: ResourceLocation,
    ): BakedModel {
        return FlexibleRealityAnchorModel()
    }
}
