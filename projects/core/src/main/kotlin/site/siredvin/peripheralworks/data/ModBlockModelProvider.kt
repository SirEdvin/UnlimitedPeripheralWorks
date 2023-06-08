package site.siredvin.peripheralworks.data

import net.minecraft.core.Direction
import net.minecraft.data.models.BlockModelGenerators
import net.minecraft.data.models.blockstates.MultiVariantGenerator
import net.minecraft.data.models.blockstates.PropertyDispatch
import net.minecraft.data.models.blockstates.Variant
import net.minecraft.data.models.blockstates.VariantProperties
import net.minecraft.data.models.model.*
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.setup.Blocks
import java.util.*

object ModBlockModelProvider {

    val PEDESTAL = ModelTemplate(
        Optional.of(ResourceLocation(PeripheralWorksCore.MOD_ID, "block/base_pedestal")),
        Optional.empty(),
        TextureSlot.TEXTURE,
        TextureSlot.TOP,
        TextureSlot.PARTICLE
    )

    private fun toYAngle(direction: Direction): VariantProperties.Rotation {
        return when (direction) {
            Direction.NORTH -> VariantProperties.Rotation.R0
            Direction.SOUTH -> VariantProperties.Rotation.R180
            Direction.EAST -> VariantProperties.Rotation.R90
            Direction.WEST -> VariantProperties.Rotation.R270
            else -> {
                VariantProperties.Rotation.R0
                VariantProperties.Rotation.R0
                VariantProperties.Rotation.R180
                VariantProperties.Rotation.R90
                VariantProperties.Rotation.R270
            }
        }
    }

    private fun toYAnglePedestal(direction: Direction): VariantProperties.Rotation {
        return when (direction) {
            Direction.NORTH -> VariantProperties.Rotation.R0
            Direction.SOUTH -> VariantProperties.Rotation.R180
            Direction.EAST -> VariantProperties.Rotation.R90
            Direction.WEST -> VariantProperties.Rotation.R270
            else -> VariantProperties.Rotation.R0
        }
    }

    private fun toXAnglePedestal(direction: Direction): VariantProperties.Rotation {
        return when (direction) {
            Direction.NORTH -> VariantProperties.Rotation.R90
            Direction.SOUTH -> VariantProperties.Rotation.R90
            Direction.EAST -> VariantProperties.Rotation.R90
            Direction.WEST -> VariantProperties.Rotation.R90
            Direction.DOWN -> VariantProperties.Rotation.R180
            Direction.UP -> VariantProperties.Rotation.R0
        }
    }

    private fun createHorizontalFacingDispatch(): PropertyDispatch {
        val dispatch = PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
        for (direction in BlockStateProperties.HORIZONTAL_FACING.possibleValues) {
            dispatch.select(
                direction,
                Variant.variant().with(
                    VariantProperties.Y_ROT,
                    toYAngle(direction)
                )
            )
        }
        return dispatch
    }

    private fun createPedestalFacingDispatch(): PropertyDispatch {
        val dispatch = PropertyDispatch.property(BlockStateProperties.FACING)
        for (direction in BlockStateProperties.FACING.possibleValues) {
            dispatch.select(
                direction,
                Variant.variant().with(
                    VariantProperties.Y_ROT,
                    toYAnglePedestal(direction)
                ).with(VariantProperties.X_ROT, toXAnglePedestal(direction))
            )
        }
        return dispatch
    }

    fun genericBlock(generators: BlockModelGenerators, block: Block) {
        val model = ModelTemplates.CUBE_ALL.create(
            block,
            TextureMapping.cube(block).put(TextureSlot.ALL, TextureMapping.getBlockTexture(block)),
            generators.modelOutput,
        )
        generators.blockStateOutput.accept(
            MultiVariantGenerator.multiVariant(
                block,
                Variant.variant().with(VariantProperties.MODEL, model),
            ),
        )
        generators.delegateItemModel(block, ModelLocationUtils.getModelLocation(block))
    }

    fun pedestalBlock(generators: BlockModelGenerators, block: Block, texture: ResourceLocation, topTexture: ResourceLocation? = null) {
        val textureMapping = TextureMapping()
        textureMapping.put(TextureSlot.TEXTURE, texture)
        textureMapping.put(TextureSlot.TOP, topTexture ?: texture)
        textureMapping.put(TextureSlot.PARTICLE, texture)
        val model = PEDESTAL.create(
            block,
            textureMapping,
            generators.modelOutput
        )
        generators.blockStateOutput.accept(
            MultiVariantGenerator.multiVariant(
                block,
                Variant.variant().with(VariantProperties.MODEL, model)
            ).with(createPedestalFacingDispatch())
        )
        generators.delegateItemModel(block, ModelLocationUtils.getModelLocation(block))
    }

    fun horizontalOrientationBlock(
        generators: BlockModelGenerators, block: Block, overwriteSide: ResourceLocation? = null,
        overwriteTop: ResourceLocation? = null, overwriteBottom: ResourceLocation? = null,
        overwriteFront: ResourceLocation? = null
    ) {
        val textureMapping = TextureMapping.orientableCube(block)
        if (overwriteSide != null)
            textureMapping.put(TextureSlot.SIDE, overwriteSide)
        if (overwriteBottom != null)
            textureMapping.put(TextureSlot.BOTTOM, overwriteBottom)
        if (overwriteTop != null)
            textureMapping.put(TextureSlot.TOP, overwriteTop)
        if (overwriteFront != null)
            textureMapping.put(TextureSlot.FRONT, overwriteFront)
        val model = ModelTemplates.CUBE_ORIENTABLE.create(
            block,
            textureMapping,
            generators.modelOutput
        )
        generators.blockStateOutput.accept(
            MultiVariantGenerator.multiVariant(block,  Variant.variant().with(VariantProperties.MODEL, model))
                .with(createHorizontalFacingDispatch())
        )
        generators.delegateItemModel(block, ModelLocationUtils.getModelLocation(block))
    }

    fun horizontalOrientationBlockWithoutModel(generators: BlockModelGenerators, block: Block) {
        generators.blockStateOutput.accept(
            MultiVariantGenerator.multiVariant(block,  Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block)))
                .with(createHorizontalFacingDispatch())
        )
        generators.delegateItemModel(block, ModelLocationUtils.getModelLocation(block))
    }

    fun addModels(generators: BlockModelGenerators) {
        val peripheralCasingTexture = TextureMapping.getBlockTexture(Blocks.PERIPHERAL_CASING.get())
        genericBlock(generators, Blocks.PERIPHERAL_CASING.get())
        horizontalOrientationBlock(generators, Blocks.REMOTE_OBSERVER.get(), overwriteBottom = peripheralCasingTexture, overwriteTop = peripheralCasingTexture)
        horizontalOrientationBlock(generators, Blocks.ULTIMATE_SENSOR.get(), overwriteBottom = peripheralCasingTexture, overwriteTop = peripheralCasingTexture)
        horizontalOrientationBlock(generators, Blocks.UNIVERSAL_SCANNER.get(), overwriteBottom = peripheralCasingTexture, overwriteTop = peripheralCasingTexture)

        pedestalBlock(generators, Blocks.ITEM_PEDESTAL.get(), TextureMapping.getBlockTexture(net.minecraft.world.level.block.Blocks.SMOOTH_STONE))
        pedestalBlock(generators, Blocks.DISPLAY_PEDESTAL.get(), TextureMapping.getBlockTexture(net.minecraft.world.level.block.Blocks.SMOOTH_STONE))
        pedestalBlock(
            generators, Blocks.MAP_PEDESTAL.get(),
            TextureMapping.getBlockTexture(net.minecraft.world.level.block.Blocks.SMOOTH_STONE),
            TextureMapping.getBlockTexture(Blocks.MAP_PEDESTAL.get(), "_top")
        )
        horizontalOrientationBlockWithoutModel(generators, Blocks.PERIPHERAL_PROXY.get())
    }
}