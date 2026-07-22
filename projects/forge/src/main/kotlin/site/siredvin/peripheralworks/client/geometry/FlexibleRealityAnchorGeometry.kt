package site.siredvin.peripheralworks.client.geometry

import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.resources.model.Material
import net.minecraft.client.resources.model.ModelBaker
import net.minecraft.client.resources.model.ModelState
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry
import site.siredvin.peripheralworks.client.model.FlexibleRealityAnchorModel
import java.util.function.Function

object FlexibleRealityAnchorGeometry : IUnbakedGeometry<FlexibleRealityAnchorGeometry> {
    override fun bake(
        context: IGeometryBakingContext,
        baker: ModelBaker,
        spriteGetter: Function<Material, TextureAtlasSprite>,
        modelState: ModelState,
        overrides: ItemOverrides,
    ): BakedModel = FlexibleRealityAnchorModel
}
