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
import net.minecraft.world.level.block.state.properties.BooleanProperty
import site.siredvin.peripheralium.data.blocks.createHorizontalFacingDispatch
import site.siredvin.peripheralium.data.blocks.genericBlock
import site.siredvin.peripheralium.data.blocks.horizontalOrientatedBlock
import site.siredvin.peripheralium.data.blocks.horizontalOrientedModel
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.block.*
import site.siredvin.peripheralworks.common.setup.Blocks
import java.util.*

object ModBlockModelProvider {

    val PEDESTAL = ModelTemplate(
        Optional.of(ResourceLocation(PeripheralWorksCore.MOD_ID, "block/base_pedestal")),
        Optional.empty(),
        TextureSlot.TEXTURE,
        TextureSlot.TOP,
        TextureSlot.PARTICLE,
    )

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

    private fun createPedestalFacingDispatch(): PropertyDispatch {
        val dispatch = PropertyDispatch.property(BlockStateProperties.FACING)
        for (direction in BlockStateProperties.FACING.possibleValues) {
            dispatch.select(
                direction,
                Variant.variant().with(
                    VariantProperties.Y_ROT,
                    toYAnglePedestal(direction),
                ).with(VariantProperties.X_ROT, toXAnglePedestal(direction)),
            )
        }
        return dispatch
    }

    private fun createPeripheralProxyDispatch(): PropertyDispatch {
        val dispatch = PropertyDispatch.property(PeripheralProxy.ORIENTATION)
        for (direction in PeripheralProxy.ORIENTATION.possibleValues) {
            dispatch.select(
                direction,
                Variant.variant().with(
                    VariantProperties.Y_ROT,
                    toYAnglePedestal(direction),
                ).with(VariantProperties.X_ROT, toXAnglePedestal(direction)),
            )
        }
        return dispatch
    }

    private fun createBooleanDispatching(offVariant: ResourceLocation, onVariant: ResourceLocation, property: BooleanProperty): PropertyDispatch {
        val dispatch = PropertyDispatch.property(property)
        dispatch.select(false, Variant.variant().with(VariantProperties.MODEL, offVariant))
        dispatch.select(true, Variant.variant().with(VariantProperties.MODEL, onVariant))
        return dispatch
    }

    fun pedestalBlock(generators: BlockModelGenerators, block: Block, texture: ResourceLocation, topTexture: ResourceLocation? = null) {
        val textureMapping = TextureMapping()
        textureMapping.put(TextureSlot.TEXTURE, texture)
        textureMapping.put(TextureSlot.TOP, topTexture ?: texture)
        textureMapping.put(TextureSlot.PARTICLE, texture)
        val model = PEDESTAL.create(
            block,
            textureMapping,
            generators.modelOutput,
        )
        generators.blockStateOutput.accept(
            MultiVariantGenerator.multiVariant(
                block,
                Variant.variant().with(VariantProperties.MODEL, model),
            ).with(createPedestalFacingDispatch()),
        )
        generators.delegateItemModel(block, ModelLocationUtils.getModelLocation(block))
    }

    fun peripheralProxyBlock(generators: BlockModelGenerators, block: Block) {
        generators.blockStateOutput.accept(
            MultiVariantGenerator.multiVariant(
                block,
                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block)),
            ).with(createPeripheralProxyDispatch()),
        )
        generators.delegateItemModel(block, ModelLocationUtils.getModelLocation(block))
    }

    fun bottomTopModel(
        generators: BlockModelGenerators,
        block: Block,
        overwriteSide: ResourceLocation? = null,
        overwriteTop: ResourceLocation? = null,
        overwriteBottom: ResourceLocation? = null,
        suffix: String = "",
    ): ResourceLocation {
        val textureMapping = TextureMapping.cubeBottomTop(block)
        if (overwriteSide != null) {
            textureMapping.put(TextureSlot.SIDE, overwriteSide)
        }
        if (overwriteBottom != null) {
            textureMapping.put(TextureSlot.BOTTOM, overwriteBottom)
        }
        if (overwriteTop != null) {
            textureMapping.put(TextureSlot.TOP, overwriteTop)
        }
        return ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(
            block,
            suffix,
            textureMapping,
            generators.modelOutput,
        )
    }

    fun simpleBlockSwitch(
        generators: BlockModelGenerators,
        block: Block,
        offModel: ResourceLocation,
        onModel: ResourceLocation,
        property: BooleanProperty,
        isItemConnected: Boolean = false,
    ) {
        generators.blockStateOutput.accept(
            MultiVariantGenerator.multiVariant(
                block,
                Variant.variant().with(VariantProperties.MODEL, offModel),
            ).with(createBooleanDispatching(offModel, onModel, property)),
        )
        generators.delegateItemModel(block, if (isItemConnected) { onModel } else { offModel })
    }

    fun facingBlockSwitch(
        generators: BlockModelGenerators,
        block: Block,
        offModel: ResourceLocation,
        onModel: ResourceLocation,
        property: BooleanProperty,
        isItemConnected: Boolean = false,
    ) {
        generators.blockStateOutput.accept(
            MultiVariantGenerator.multiVariant(
                block,
                Variant.variant().with(VariantProperties.MODEL, offModel),
            ).with(createBooleanDispatching(offModel, onModel, property)).with(createHorizontalFacingDispatch()),
        )
        generators.delegateItemModel(block, if (isItemConnected) { onModel } else { offModel })
    }

    fun addModels(generators: BlockModelGenerators) {
        val peripheralCasingTexture = TextureMapping.getBlockTexture(Blocks.PERIPHERAL_CASING.get())
        genericBlock(generators, Blocks.PERIPHERAL_CASING.get())
        horizontalOrientatedBlock(
            generators,
            Blocks.REMOTE_OBSERVER.get(),
            horizontalOrientedModel(generators, Blocks.REMOTE_OBSERVER.get(), overwriteBottom = peripheralCasingTexture, overwriteTop = peripheralCasingTexture),
        )
        horizontalOrientatedBlock(
            generators,
            Blocks.ULTIMATE_SENSOR.get(),
            horizontalOrientedModel(generators, Blocks.ULTIMATE_SENSOR.get(), overwriteBottom = peripheralCasingTexture, overwriteTop = peripheralCasingTexture),
        )
        horizontalOrientatedBlock(
            generators,
            Blocks.UNIVERSAL_SCANNER.get(),
//            horizontalOrientedModel(generators, Blocks.UNIVERSAL_SCANNER.get(), overwriteBottom = peripheralCasingTexture, overwriteTop = peripheralCasingTexture),
        )

        pedestalBlock(generators, Blocks.ITEM_PEDESTAL.get(), TextureMapping.getBlockTexture(net.minecraft.world.level.block.Blocks.SMOOTH_STONE))
        pedestalBlock(generators, Blocks.DISPLAY_PEDESTAL.get(), TextureMapping.getBlockTexture(net.minecraft.world.level.block.Blocks.SMOOTH_STONE))
        pedestalBlock(
            generators,
            Blocks.MAP_PEDESTAL.get(),
            TextureMapping.getBlockTexture(net.minecraft.world.level.block.Blocks.SMOOTH_STONE),
            TextureMapping.getBlockTexture(Blocks.MAP_PEDESTAL.get(), "_top"),
        )
        peripheralProxyBlock(generators, Blocks.PERIPHERAL_PROXY.get())
        horizontalOrientatedBlock(
            generators,
            Blocks.REALITY_FORGER.get(),
            horizontalOrientedModel(
                generators,
                Blocks.REALITY_FORGER.get(),
                overwriteBottom = peripheralCasingTexture,
                overwriteTop = peripheralCasingTexture,

            ),
        )

        simpleBlockSwitch(
            generators,
            Blocks.FLEXIBLE_REALITY_ANCHOR.get(),
            ModelLocationUtils.getModelLocation(Blocks.FLEXIBLE_REALITY_ANCHOR.get(), "_empty"),
            ModelLocationUtils.getModelLocation(Blocks.FLEXIBLE_REALITY_ANCHOR.get()),
            FlexibleRealityAnchor.CONFIGURED,
            isItemConnected = true,
        )

        horizontalOrientatedBlock(
            generators,
            Blocks.RECIPE_REGISTRY.get(),
            horizontalOrientedModel(
                generators,
                Blocks.RECIPE_REGISTRY.get(),
                overwriteTop = peripheralCasingTexture,
                overwriteBottom = peripheralCasingTexture,
                overwriteFront = TextureMapping.getBlockTexture(Blocks.RECIPE_REGISTRY.get(), "_side"),
            ),
        )

        horizontalOrientatedBlock(
            generators,
            Blocks.INFORMATIVE_REGISTRY.get(),
            horizontalOrientedModel(
                generators,
                Blocks.INFORMATIVE_REGISTRY.get(),
                overwriteTop = peripheralCasingTexture,
                overwriteBottom = peripheralCasingTexture,
                overwriteFront = TextureMapping.getBlockTexture(Blocks.INFORMATIVE_REGISTRY.get(), "_side"),
            ),
        )

        simpleBlockSwitch(
            generators,
            Blocks.STATUE_WORKBENCH.get(),
            bottomTopModel(
                generators,
                Blocks.STATUE_WORKBENCH.get(),
                overwriteBottom = peripheralCasingTexture,
                overwriteTop = peripheralCasingTexture,
            ),
            bottomTopModel(
                generators,
                Blocks.STATUE_WORKBENCH.get(),
                overwriteSide = TextureMapping.getBlockTexture(Blocks.STATUE_WORKBENCH.get()).withSuffix("_side_connected"),
                overwriteBottom = peripheralCasingTexture,
                overwriteTop = peripheralCasingTexture,
                suffix = "_connected",
            ),
            StatueWorkbench.CONNECTED,
        )

        simpleBlockSwitch(
            generators,
            Blocks.ENTITY_LINK.get(),
            ModelLocationUtils.getModelLocation(Blocks.ENTITY_LINK.get()),
            ModelLocationUtils.getModelLocation(Blocks.ENTITY_LINK.get()).withSuffix("_filled"),
            EntityLink.CONFIGURED,
        )

        facingBlockSwitch(
            generators,
            Blocks.FLEXIBLE_STATUE.get(),
            ModelLocationUtils.getModelLocation(Blocks.FLEXIBLE_STATUE.get(), "_empty"),
            ModelLocationUtils.getModelLocation(Blocks.FLEXIBLE_STATUE.get()),
            FlexibleStatue.CONFIGURED,
            isItemConnected = true,
        )
    }
}
