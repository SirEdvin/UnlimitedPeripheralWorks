package site.siredvin.peripheralworks.client.model

import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.block.model.*
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.resources.model.ModelState
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.client.model.IDynamicBakedModel
import net.minecraftforge.client.model.data.ModelData
import net.minecraftforge.client.model.data.ModelProperty
import site.siredvin.peripheralium.common.blocks.BaseNBTBlock
import site.siredvin.peripheralworks.client.util.RenderUtils
import site.siredvin.peripheralworks.client.util.RenderUtils.getModelState
import site.siredvin.peripheralworks.client.util.RenderUtils.getTexture
import site.siredvin.peripheralworks.common.blockentity.FlexibleStatueBlockEntity
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.utils.QuadData
import site.siredvin.peripheralworks.utils.QuadList
import site.siredvin.peripheralworks.utils.modId
import java.util.stream.Collectors

val emptyFlexibleStatueModel by lazy {
    Minecraft.getInstance().blockRenderer.getBlockModel(Blocks.FLEXIBLE_STATUE.get().defaultBlockState())
}

val identityModel by lazy {
    object : ModelState {}
}

abstract class AbstractFlexibleStatueModel : IDynamicBakedModel {
    companion object {
        val DEFAULT_TEXTURE = modId("block/white")
        val DUMMY = ResourceLocation("dummy_name")
    }

    protected fun bake(bakery: FaceBakery, data: QuadData, side: Direction, modelState: ModelState = identityModel): BakedQuad {
        val face = BlockElementFace(
            null,
            -1,
            "",
            BlockFaceUV(data.uv, 0),
        )
        val quad = bakery.bakeQuad(
            data.start, data.end, face, getTexture(data.texture), side,
            modelState, null, true, DUMMY,
        )
        quad.vertices[3] = data.tint
        quad.vertices[11] = data.tint
        quad.vertices[19] = data.tint
        quad.vertices[27] = data.tint
        return quad
    }

    override fun useAmbientOcclusion(): Boolean = true
    override fun getParticleIcon(): TextureAtlasSprite = getTexture(DEFAULT_TEXTURE)
    override fun isGui3d(): Boolean = true
    override fun usesBlockLight(): Boolean = false
    override fun isCustomRenderer(): Boolean = false
}

object FlexibleStatueModel : AbstractFlexibleStatueModel() {
    val QUADS = ModelProperty<QuadList>()
    val FACING = ModelProperty<Direction>()
    override fun getQuads(
        state: BlockState?,
        side: Direction?,
        rand: RandomSource,
        extraData: ModelData,
        renderType: RenderType?,
    ): MutableList<BakedQuad> {
        val quadsData = extraData.get(QUADS) ?: return emptyFlexibleStatueModel.getQuads(state, side, rand, extraData, renderType)
        val safeSide = side ?: Direction.SOUTH
        val rotation = when (extraData.get(FACING) ?: Direction.EAST) {
            Direction.SOUTH -> Axis.YP.rotationDegrees(180f)
            Direction.WEST -> Axis.YP.rotationDegrees(90f)
            Direction.EAST -> Axis.YN.rotationDegrees(90f)
            else -> Axis.YP.rotationDegrees(0f)
        }
        val bakery = FaceBakery()
        return quadsData.list.stream().map { data -> bake(bakery, data, safeSide, getModelState(rotation)) }.collect(Collectors.toList())
    }

    override fun getOverrides(): ItemOverrides = FlexibleStatueItemOverrides

    override fun getModelData(
        level: BlockAndTintGetter,
        pos: BlockPos,
        state: BlockState,
        modelData: ModelData,
    ): ModelData {
        val blockEntity = level.getBlockEntity(pos)
        if (blockEntity !is FlexibleStatueBlockEntity || blockEntity.bakedQuads == null) return super.getModelData(level, pos, state, modelData)
        return modelData.derive().with(QUADS, blockEntity.bakedQuads).with(FACING, blockEntity.facing).build()
    }
}

object FlexibleStatueItemOverrides : ItemOverrides() {
    override fun resolve(
        pModel: BakedModel,
        pStack: ItemStack,
        pLevel: ClientLevel?,
        pEntity: LivingEntity?,
        pSeed: Int,
    ): BakedModel? {
        val bakedQuadsTag = pStack.getTagElement(BaseNBTBlock.INTERNAL_DATA_TAG)?.getList(
            FlexibleStatueBlockEntity.BAKED_QUADS_TAG,
            10,
        ) ?: return emptyFlexibleStatueModel
        if (bakedQuadsTag.isEmpty()) return emptyFlexibleStatueModel
        val quadList = QuadList(bakedQuadsTag)
        return ItemFlexibleStatueModel(quadList)
    }
}

class ItemFlexibleStatueModel(private val quads: QuadList) : AbstractFlexibleStatueModel() {
    override fun getQuads(
        state: BlockState?,
        side: Direction?,
        rand: RandomSource,
        extraData: ModelData,
        renderType: RenderType?,
    ): MutableList<BakedQuad> {
        val bakery = FaceBakery()
        return quads.list.stream().map { data -> bake(bakery, data, side ?: Direction.SOUTH) }.collect(Collectors.toList())
    }

    override fun getOverrides(): ItemOverrides = ItemOverrides.EMPTY
    override fun getTransforms(): ItemTransforms = RenderUtils.BLOCK_TRANSFORMS
}
