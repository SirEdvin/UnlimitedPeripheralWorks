package site.siredvin.peripheralworks.client

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView.BAKE_LOCK_UV
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.*
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.*
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralium.common.blocks.BaseNBTBlock
import site.siredvin.peripheralium.ext.faces
import site.siredvin.peripheralium.ext.rotateTowards
import site.siredvin.peripheralworks.common.blockentity.FlexibleStatueBlockEntity
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.utils.*
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

@Environment(EnvType.CLIENT)
object FlexibleStatueModel : BakedModel, FabricBakedModel {
    private val EMPTY_TEXTURE = modId("block/white")
    private val meshCache = CacheBuilder.newBuilder()
        .concurrencyLevel(1).expireAfterAccess(30, TimeUnit.SECONDS)
        .maximumSize(2_000).build(CacheLoader.from(::buildPrintMesh))

    val defaultItemModel by lazy {
        Minecraft.getInstance().blockRenderer.getBlockModel(Blocks.FLEXIBLE_STATUE.get().defaultBlockState())
    }

    val texture: TextureAtlasSprite by lazy {
        RenderUtils.getTexture(EMPTY_TEXTURE)
    }

    override fun getQuads(
        blockState: BlockState?,
        direction: Direction?,
        randomSource: RandomSource,
    ): MutableList<BakedQuad> {
        return mutableListOf()
    }

    override fun useAmbientOcclusion(): Boolean = true
    override fun isGui3d(): Boolean = true
    override fun usesBlockLight(): Boolean = false
    override fun isCustomRenderer(): Boolean = false
    override fun getParticleIcon(): TextureAtlasSprite = texture
    override fun getTransforms(): ItemTransforms = ModelHelper.MODEL_TRANSFORM_BLOCK
    override fun getOverrides(): ItemOverrides = ItemOverrides.EMPTY
    override fun isVanillaAdapter(): Boolean = false

    private fun buildShape(emitter: QuadEmitter, quad: QuadData, facing: Direction) {
        // Render inspired by https://github.com/SwitchCraftCC/sc-peripherals/blob/1.20.1/src/main/kotlin/io/sc3/peripherals/client/block/PrintBakedModel.kt logic
        val bounds = quad.toAABB().rotateTowards(facing)

        val alpha = ((quad.opacity * 255f) + 0.5).toInt()
        val tint = (quad.tint and 0xFFFFFF) or (alpha shl 24)
        val material = Material(BLOCK_ATLAS, quad.texture)
        val sprite = material.sprite()

        // Generate the 6 faces for this Box, as a list of four vertices
        val faces = bounds.faces

        // Render each quad of the cube
        for (dir in Direction.values()) {
            // TODO: Since we are not using QuadEmitter.square(), we need to do the cullFace check ourselves
            val face = faces[dir.ordinal]
            emitter.cullFace(null)
            emitter.nominalFace(dir)
            emitter
                .pos(0, face[0])
                .pos(1, face[1])
                .pos(2, face[2])
                .pos(3, face[3])
                .color(0, tint)
                .color(1, tint)
                .color(2, tint)
                .color(3, tint)
                .spriteBake(sprite, BAKE_LOCK_UV)
                .emit()
        }
    }

    private fun buildPrintMesh(pair: Pair<QuadList, Direction>): Mesh {
        val renderer = RendererAccess.INSTANCE.renderer!!
        val builder = renderer.meshBuilder()
        val emitter = builder.emitter

        pair.first.list.forEach {
            buildShape(emitter, it, pair.second)
        }
        return builder.build()
    }

    override fun emitBlockQuads(
        blockView: BlockAndTintGetter,
        state: BlockState,
        pos: BlockPos,
        randomSupplier: Supplier<RandomSource>,
        context: RenderContext,
    ) {
        val entity = blockView.getBlockEntity(pos)
        if (entity !is FlexibleStatueBlockEntity) {
            return
        }
        val quads = entity.bakedQuads ?: return
        val mesh = meshCache.get(Pair(quads, entity.facing))

        mesh.outputTo(context.emitter)
    }

    fun emitDefaultItemQuads(stack: ItemStack, randomSupplier: Supplier<RandomSource>, context: RenderContext) {
        defaultItemModel.emitItemQuads(stack, randomSupplier, context)
    }

    override fun emitItemQuads(stack: ItemStack, randomSupplier: Supplier<RandomSource>, context: RenderContext) {
        if (!stack.`is`(Blocks.FLEXIBLE_STATUE.get().asItem())) {
            return emitDefaultItemQuads(stack, randomSupplier, context)
        }
        val quadList = stack.getTagElement(BaseNBTBlock.INTERNAL_DATA_TAG)?.getList(FlexibleStatueBlockEntity.BAKED_QUADS_TAG, 10) ?: return emitDefaultItemQuads(stack, randomSupplier, context)
        if (quadList.isEmpty()) return emitDefaultItemQuads(stack, randomSupplier, context)
        val quads = QuadList(quadList)
        val mesh = meshCache.get(Pair(quads, Direction.SOUTH))
        mesh.outputTo(context.emitter)
    }
}
