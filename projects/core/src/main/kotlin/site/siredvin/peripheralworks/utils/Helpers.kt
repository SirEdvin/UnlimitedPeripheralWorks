package site.siredvin.peripheralworks.utils

import net.minecraft.data.models.BlockModelGenerators
import net.minecraft.data.models.model.ModelTemplates
import net.minecraft.data.models.model.TextureMapping
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import site.siredvin.peripheralworks.PeripheralWorksCore

@Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
fun modId(text: String): ResourceLocation = ResourceLocation(PeripheralWorksCore.MOD_ID, text)

fun horizontalOrientedModelWithOverride(
    generators: BlockModelGenerators,
    block: Block,
    suffix: String,
    overwriteSide: ResourceLocation? = null,
    overwriteTop: ResourceLocation? = null,
    overwriteBottom: ResourceLocation? = null,
    overwriteFront: ResourceLocation? = null,
): ResourceLocation {
    val textureMapping = TextureMapping.orientableCube(block)
    if (overwriteSide != null) {
        textureMapping.put(TextureSlot.SIDE, overwriteSide)
    }
    if (overwriteBottom != null) {
        textureMapping.put(TextureSlot.BOTTOM, overwriteBottom)
    }
    if (overwriteTop != null) {
        textureMapping.put(TextureSlot.TOP, overwriteTop)
    }
    if (overwriteFront != null) {
        textureMapping.put(TextureSlot.FRONT, overwriteFront)
    }
    return ModelTemplates.CUBE_ORIENTABLE.createWithOverride(
        block,
        suffix,
        textureMapping,
        generators.modelOutput,
    )
}
