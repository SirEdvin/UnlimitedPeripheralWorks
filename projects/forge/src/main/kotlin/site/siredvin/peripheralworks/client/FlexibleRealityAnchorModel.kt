package site.siredvin.peripheralworks.client

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.core.Direction
import net.minecraft.nbt.NbtUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.client.model.IDynamicBakedModel
import net.minecraftforge.client.model.data.ModelData
import site.siredvin.peripheralium.common.blocks.BaseNBTBlock
import site.siredvin.peripheralium.xplat.XplatRegistries
import site.siredvin.peripheralworks.common.block.FlexibleRealityAnchor
import site.siredvin.peripheralworks.common.blockentity.FlexibleRealityAnchorBlockEntity
import site.siredvin.peripheralworks.common.setup.Blocks

class FlexibleRealityAnchorModel : IDynamicBakedModel {

    val emptyModel by lazy {
        Minecraft.getInstance().blockRenderer.getBlockModel(Blocks.FLEXIBLE_REALITY_ANCHOR.get().defaultBlockState())
    }

    private fun getTexture(): TextureAtlasSprite {
        return(
            Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(ResourceLocation("minecraft:block/stone")) as TextureAtlasSprite
            )
    }

    override fun getQuads(
        state: BlockState?,
        side: Direction?,
        rand: RandomSource,
        extraData: ModelData,
        renderType: RenderType?,
    ): MutableList<BakedQuad> {
        val mimic = extraData.get(FlexibleRealityAnchorBlockEntity.MIMIC)
        if (mimic != null && mimic.block !is FlexibleRealityAnchor) {
            val model: BakedModel = Minecraft.getInstance().blockRenderer.getBlockModel(mimic)
            return model.getQuads(state, side, rand, extraData, renderType)
        }
        return mutableListOf()
    }

    fun getDefaultRenderPasses(): MutableList<BakedModel> {
        return mutableListOf(
            emptyModel,
        )
    }

    override fun getRenderPasses(itemStack: ItemStack, fabulous: Boolean): MutableList<BakedModel> {
        if (!itemStack.`is`(Blocks.FLEXIBLE_REALITY_ANCHOR.get().asItem())) {
            return getDefaultRenderPasses()
        }
        val mimicBlockStateTag = itemStack.getTagElement(BaseNBTBlock.INTERNAL_DATA_TAG)?.getCompound(FlexibleRealityAnchorBlockEntity.MIMIC_TAG) ?: return getDefaultRenderPasses()
        val mimicBlockState = NbtUtils.readBlockState(XplatRegistries.BLOCKS, mimicBlockStateTag)
        if (mimicBlockState.isAir) return getDefaultRenderPasses()
        return mutableListOf(Minecraft.getInstance().blockRenderer.getBlockModel(mimicBlockState))
    }

    override fun useAmbientOcclusion(): Boolean {
        return true
    }

    override fun isGui3d(): Boolean {
        return false
    }

    override fun usesBlockLight(): Boolean {
        return false
    }

    override fun isCustomRenderer(): Boolean {
        return false
    }

    override fun getParticleIcon(): TextureAtlasSprite {
        return getTexture()
    }

    override fun getTransforms(): ItemTransforms {
        return emptyModel.transforms
    }

    override fun getOverrides(): ItemOverrides {
        return ItemOverrides.EMPTY
    }
}
