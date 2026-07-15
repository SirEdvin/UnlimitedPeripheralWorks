package site.siredvin.peripheralworks.integrations.kubejs

import dev.latvian.mods.kubejs.block.BlockBuilder
import dev.latvian.mods.kubejs.block.custom.ShapedBlockBuilder
import dev.latvian.mods.kubejs.block.entity.BlockEntityInfo
import dev.latvian.mods.kubejs.client.VariantBlockStateGenerator
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator
import dev.latvian.mods.kubejs.registry.AdditionalObjectRegistry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.PeripheralWorksCore

abstract class AbstractPedestalBuilder(i: ResourceLocation) : ShapedBlockBuilder(i) {
    companion object {
        val BASE_MODEL = ResourceLocation.tryBuild(PeripheralWorksCore.MOD_ID, "block/base_pedestal")
    }

    init {
        texture(arrayOf("particle"), "minecraft:block/smooth_stone")
        texture(arrayOf("texture"), "minecraft:block/smooth_stone")
        texture(arrayOf("top"), "minecraft:block/smooth_stone")
        requiresTool(false)
    }

    abstract fun buildBlockEntityInfo(): BlockEntityInfo

    fun textureAll(tex: String): BlockBuilder {
        texture(arrayOf("particle"), tex)
        texture(arrayOf("texture"), tex)
        texture(arrayOf("top"), tex)
        return this
    }

    override fun createAdditionalObjects(registry: AdditionalObjectRegistry) {
        val pedestalItemBuilder = itemBuilder
        if (pedestalItemBuilder != null) {
            registry.add(Registries.ITEM, pedestalItemBuilder)
        }
        blockEntityInfo = buildBlockEntityInfo()
        registry.add(Registries.BLOCK_ENTITY_TYPE, PedestalBlockEntityBuilder(id, blockEntityInfo))
    }

    override fun generateBlockModels(generator: KubeAssetGenerator) {
        generator.blockModel(id, { mg ->
            mg.parent(BASE_MODEL)
            mg.textures(textures)
        })
    }

    override fun generateBlockState(bs: VariantBlockStateGenerator) {
        val mod0 = id.withPrefix("block/")
        bs.variant("facing=down", { it.model(mod0).x(180).y(0) })
        bs.variant("facing=east", { it.model(mod0).x(90).y(90) })
        bs.variant("facing=north", { it.model(mod0).x(90).y(0) })
        bs.variant("facing=south", { it.model(mod0).x(90).y(180) })
        bs.variant("facing=up", { it.model(mod0).x(0).y(0) })
        bs.variant("facing=west", { it.model(mod0).x(90).y(270) })
    }
}
