package site.siredvin.peripheralworks.integrations.kubejs

import dev.latvian.mods.kubejs.block.BlockBuilder
import dev.latvian.mods.kubejs.block.custom.ShapedBlockBuilder
import dev.latvian.mods.kubejs.block.entity.BlockEntityInfo
import dev.latvian.mods.kubejs.client.VariantBlockStateGenerator
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator
import dev.latvian.mods.kubejs.registry.RegistryInfo
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.PeripheralWorksCore

abstract class AbstractPedestalBuilder(i: ResourceLocation) : ShapedBlockBuilder(i) {
    companion object {
        val BASE_MODEL = ResourceLocation.tryBuild(PeripheralWorksCore.MOD_ID, "block/base_pedestal")
    }

    init {
        texture("particle", "minecraft:block/smooth_stone")
        texture("texture", "minecraft:block/smooth_stone")
        texture("top", "minecraft:block/smooth_stone")
        requiresTool(false)
    }

    abstract fun buildBlockEntityInfo(): BlockEntityInfo

    override fun textureAll(tex: String): BlockBuilder {
        texture("particle", tex)
        texture("texture", tex)
        texture("top", tex)
        return this
    }

    override fun createAdditionalObjects() {
        if (itemBuilder != null) {
            RegistryInfo.ITEM.addBuilder(itemBuilder)
        }
        blockEntityInfo = buildBlockEntityInfo()
        RegistryInfo.BLOCK_ENTITY_TYPE.addBuilder(PedestalBlockEntityBuilder(id, blockEntityInfo))
    }

    override fun generateBlockModelJsons(generator: AssetJsonGenerator) {
        generator.blockModel(id, { mg ->
            mg.parent(BASE_MODEL.toString())
            mg.textures(textures)
        })
    }

    override fun generateBlockStateJson(bs: VariantBlockStateGenerator) {
        val mod0 = model.ifEmpty { id.namespace + ":block/" + id.path }
        bs.variant("facing=down", { it.model(mod0).x(180).y(0) })
        bs.variant("facing=east", { it.model(mod0).x(90).y(90) })
        bs.variant("facing=north", { it.model(mod0).x(90).y(0) })
        bs.variant("facing=south", { it.model(mod0).x(90).y(180) })
        bs.variant("facing=up", { it.model(mod0).x(0).y(0) })
        bs.variant("facing=west", { it.model(mod0).x(90).y(270) })
    }
}
