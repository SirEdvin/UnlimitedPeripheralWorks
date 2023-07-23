package site.siredvin.peripheralworks.client.model

import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.NbtUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.client.ChunkRenderTypeSet
import net.minecraftforge.client.model.IDynamicBakedModel
import net.minecraftforge.client.model.data.ModelData
import net.minecraftforge.client.model.data.ModelProperty
import site.siredvin.peripheralium.common.blocks.BaseNBTBlock
import site.siredvin.peripheralium.xplat.XplatRegistries
import site.siredvin.peripheralworks.client.util.RenderUtils.getTexture
import site.siredvin.peripheralworks.common.block.FlexibleRealityAnchor
import site.siredvin.peripheralworks.common.blockentity.FlexibleRealityAnchorBlockEntity
import site.siredvin.peripheralworks.common.setup.Blocks

val emptyFlexibleRealityAnchorModel by lazy {
    Minecraft.getInstance().blockRenderer.getBlockModel(Blocks.FLEXIBLE_REALITY_ANCHOR.get().defaultBlockState())
}

object FlexibleRealityAnchorModel : IDynamicBakedModel {
    val MIMIC = ModelProperty<BlockState>()

    override fun getQuads(
        state: BlockState?,
        side: Direction?,
        rand: RandomSource,
        extraData: ModelData,
        renderType: RenderType?,
    ): MutableList<BakedQuad> {
        val mimic = extraData.get(MIMIC)
        if (mimic != null && mimic.block !is FlexibleRealityAnchor) {
            val model: BakedModel = Minecraft.getInstance().blockRenderer.getBlockModel(mimic)
            return model.getQuads(mimic, side, rand, extraData, renderType)
        }
        return mutableListOf()
    }

    override fun useAmbientOcclusion(): Boolean = true
    override fun isGui3d(): Boolean = true
    override fun usesBlockLight(): Boolean = false
    override fun isCustomRenderer(): Boolean = false
    override fun getRenderTypes(state: BlockState, rand: RandomSource, data: ModelData): ChunkRenderTypeSet {
        return ChunkRenderTypeSet.of(RenderType.cutout())
    }

    @Deprecated("Deprecated in Java")
    override fun getParticleIcon(): TextureAtlasSprite = getTexture(ResourceLocation("minecraft:block/stone"))

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun getTransforms(): ItemTransforms = emptyFlexibleRealityAnchorModel.transforms
    override fun getOverrides(): ItemOverrides = FlexibleRealityAnchorItemOverrides

    override fun getModelData(
        level: BlockAndTintGetter,
        pos: BlockPos,
        state: BlockState,
        modelData: ModelData,
    ): ModelData {
        val blockEntity = level.getBlockEntity(pos)
        if (blockEntity !is FlexibleRealityAnchorBlockEntity || blockEntity.mimic == null) return super.getModelData(level, pos, state, modelData)
        return modelData.derive().with(MIMIC, blockEntity.mimic).build()
    }
}

object FlexibleRealityAnchorItemOverrides : ItemOverrides() {
    override fun resolve(
        pModel: BakedModel,
        pStack: ItemStack,
        pLevel: ClientLevel?,
        pEntity: LivingEntity?,
        pSeed: Int,
    ): BakedModel? {
        val mimic = pStack.getTagElement(BaseNBTBlock.INTERNAL_DATA_TAG)?.getCompound(
            FlexibleRealityAnchorBlockEntity.MIMIC_TAG,
        ) ?: return emptyFlexibleRealityAnchorModel
        if (mimic.isEmpty) return emptyFlexibleRealityAnchorModel
        val mimicState = NbtUtils.readBlockState(XplatRegistries.BLOCKS, mimic)
        if (mimicState.isAir) return emptyFlexibleRealityAnchorModel
        return Minecraft.getInstance().blockRenderer.getBlockModel(mimicState)
    }
}
