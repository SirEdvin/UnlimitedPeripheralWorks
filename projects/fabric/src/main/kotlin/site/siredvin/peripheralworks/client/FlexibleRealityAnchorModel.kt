package site.siredvin.peripheralworks.client

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.*
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.NbtUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralium.common.blocks.BaseNBTBlock
import site.siredvin.peripheralium.xplat.XplatRegistries
import site.siredvin.peripheralworks.common.blockentity.FlexibleRealityAnchorBlockEntity
import site.siredvin.peripheralworks.common.setup.Blocks
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

@Environment(EnvType.CLIENT)
class FlexibleRealityAnchorModel : UnbakedModel, BakedModel, FabricBakedModel {
    override fun getQuads(
        blockState: BlockState?,
        direction: Direction?,
        randomSource: RandomSource,
    ): MutableList<BakedQuad> {
        return mutableListOf()
    }

    override fun useAmbientOcclusion(): Boolean {
        return true
    }

    override fun isGui3d(): Boolean {
        return false
    }

    override fun usesBlockLight(): Boolean {
        return true
    }

    override fun isCustomRenderer(): Boolean {
        return true
    }

    override fun getParticleIcon(): TextureAtlasSprite {
        return (
            Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(ResourceLocation("minecraft:block/stone")) as TextureAtlasSprite
            )
    }

    override fun getTransforms(): ItemTransforms {
        return ModelHelper.MODEL_TRANSFORM_BLOCK
    }

    override fun getOverrides(): ItemOverrides {
        return ItemOverrides.EMPTY
    }

    override fun isVanillaAdapter(): Boolean {
        return false
    }

    override fun emitBlockQuads(
        blockView: BlockAndTintGetter,
        state: BlockState,
        pos: BlockPos,
        randomSupplier: Supplier<RandomSource>,
        context: RenderContext,
    ) {
        val entity = blockView.getBlockEntity(pos)
        if (entity !is FlexibleRealityAnchorBlockEntity) {
            return
        }
        val mimicBlockState = entity.mimic ?: return
        val bakedModel = Minecraft.getInstance().blockRenderer.getBlockModel(mimicBlockState)
        context.bakedModelConsumer().accept(bakedModel)
    }

    fun emitDefaultItemQuads(context: RenderContext) {
        context.bakedModelConsumer().accept(Minecraft.getInstance().blockRenderer.getBlockModel(Blocks.FLEXIBLE_REALITY_ANCHOR.get().defaultBlockState()))
    }

    override fun emitItemQuads(stack: ItemStack, randomSupplier: Supplier<RandomSource>, context: RenderContext) {
        if (!stack.`is`(Blocks.FLEXIBLE_REALITY_ANCHOR.get().asItem())) {
            return emitDefaultItemQuads(context)
        }
        val mimicBlockStateTag = stack.getTagElement(BaseNBTBlock.INTERNAL_DATA_TAG)?.getCompound(FlexibleRealityAnchorBlockEntity.MIMIC_TAG) ?: return emitDefaultItemQuads(context)
        val mimicBlockState = NbtUtils.readBlockState(XplatRegistries.BLOCKS, mimicBlockStateTag)
        if (mimicBlockState.isAir) return emitDefaultItemQuads(context)
        val bakedModel = Minecraft.getInstance().blockRenderer.getBlockModel(mimicBlockState)
        context.bakedModelConsumer().accept(bakedModel)
    }

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
        return this
    }
}