package site.siredvin.peripheralworks.client

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
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.*
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralium.common.blocks.BaseNBTBlock
import site.siredvin.peripheralworks.common.blockentity.FlexibleStatueBlockEntity
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.utils.*
import java.util.*
import java.util.function.Supplier

@Environment(EnvType.CLIENT)
object FlexibleStatueModel : BakedModel, FabricBakedModel {
    private val EMPTY_TEXTURE = modId("block/flexible_statue_empty")

    val textureAtlas by lazy {
        Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
    }

    val defaultItemModel by lazy {
        Minecraft.getInstance().blockRenderer.getBlockModel(Blocks.FLEXIBLE_STATUE.get().defaultBlockState())
    }

    val texture: TextureAtlasSprite
        get() = textureAtlas.apply(EMPTY_TEXTURE)

    fun extractTexture(id: ResourceLocation): TextureAtlasSprite {
        return textureAtlas.apply(id)
    }

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
        return extractTexture(ResourceLocation("minecraft:block/stone"))
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

    private fun buildShape(emitter: QuadEmitter, quad: QuadData, facing: Direction) {
        // Render inspired by https://github.com/SwitchCraftCC/sc-peripherals/blob/1.20.1/src/main/kotlin/io/sc3/peripherals/client/block/PrintBakedModel.kt logic
        val bounds = quad.toAABB().rotateTowards(facing)

        // Discard original alpha component, then set it back to full alpha
        val tint = (quad.tint and 0xFFFFFF) or 0xFF000000.toInt()
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
                .spriteBake(sprite, BAKE_LOCK_UV)
                .color(0, tint)
                .color(1, tint)
                .color(2, tint)
                .color(3, tint)
                .emit()
        }
    }

    private fun buildPrintMesh(list: QuadList, facing: Direction = Direction.SOUTH): Mesh? {
        val renderer = RendererAccess.INSTANCE.renderer ?: return null
        val builder = renderer.meshBuilder()
        val emitter = builder.emitter

        list.list.forEach {
            buildShape(emitter, it, facing)
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
        val mesh = buildPrintMesh(quads, entity.facing)
        if (mesh != null) context.meshConsumer().accept(mesh)
    }

    fun emitDefaultItemQuads(context: RenderContext) {
        context.bakedModelConsumer().accept(defaultItemModel)
    }

    override fun emitItemQuads(stack: ItemStack, randomSupplier: Supplier<RandomSource>, context: RenderContext) {
        if (!stack.`is`(Blocks.FLEXIBLE_STATUE.get().asItem())) {
            return emitDefaultItemQuads(context)
        }
        val quadList = stack.getTagElement(BaseNBTBlock.INTERNAL_DATA_TAG)?.getList(FlexibleStatueBlockEntity.BAKED_QUADS_TAG, 10) ?: return emitDefaultItemQuads(context)
        if (quadList.isEmpty()) return emitDefaultItemQuads(context)
        val quads = QuadList(quadList)
        val mesh = buildPrintMesh(quads) ?: return emitDefaultItemQuads(context)
        context.meshConsumer().accept(mesh)
    }
}
