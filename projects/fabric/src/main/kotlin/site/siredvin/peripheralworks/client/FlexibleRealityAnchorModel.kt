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
import java.util.function.Supplier

@Environment(EnvType.CLIENT)
object FlexibleRealityAnchorModel : BakedModel, FabricBakedModel {

    val particleTextureId = ResourceLocation("minecraft:block/stone")
    val defaultItemModel by lazy {
        Minecraft.getInstance().blockRenderer.getBlockModel(Blocks.FLEXIBLE_REALITY_ANCHOR.get().defaultBlockState())
    }
    val particleTexture by lazy {
        RenderUtils.getTexture(particleTextureId)
    }

    override fun getQuads(
        blockState: BlockState?,
        direction: Direction?,
        randomSource: RandomSource,
    ): MutableList<BakedQuad> {
        return mutableListOf()
    }
    override fun useAmbientOcclusion(): Boolean = true
    override fun isGui3d(): Boolean = false
    override fun usesBlockLight(): Boolean = true
    override fun isCustomRenderer(): Boolean = false
    override fun getParticleIcon(): TextureAtlasSprite = particleTexture
    override fun getTransforms(): ItemTransforms = ModelHelper.MODEL_TRANSFORM_BLOCK
    override fun getOverrides(): ItemOverrides = ItemOverrides.EMPTY
    override fun isVanillaAdapter(): Boolean = false

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
        bakedModel.emitBlockQuads(blockView, mimicBlockState, pos, randomSupplier, context)
    }

    fun emitDefaultItemQuads(stack: ItemStack, randomSupplier: Supplier<RandomSource>, context: RenderContext) {
        defaultItemModel.emitItemQuads(stack, randomSupplier, context)
    }

    override fun emitItemQuads(stack: ItemStack, randomSupplier: Supplier<RandomSource>, context: RenderContext) {
        if (!stack.`is`(Blocks.FLEXIBLE_REALITY_ANCHOR.get().asItem())) {
            return emitDefaultItemQuads(stack, randomSupplier, context)
        }
        val mimicBlockStateTag = stack.getTagElement(BaseNBTBlock.INTERNAL_DATA_TAG)?.getCompound(FlexibleRealityAnchorBlockEntity.MIMIC_TAG) ?: return emitDefaultItemQuads(stack, randomSupplier, context)
        val mimicBlockState = NbtUtils.readBlockState(XplatRegistries.BLOCKS, mimicBlockStateTag)
        if (mimicBlockState.isAir) return emitDefaultItemQuads(stack, randomSupplier, context)
        val bakedModel = Minecraft.getInstance().blockRenderer.getBlockModel(mimicBlockState)
        bakedModel.emitItemQuads(mimicBlockState.block.asItem().defaultInstance, randomSupplier, context)
    }
}
