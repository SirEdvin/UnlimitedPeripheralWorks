package site.siredvin.peripheralworks

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.client.model.loading.v1.ModelResolver
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.client.FlexibleRealityAnchorUnbakedModel
import site.siredvin.peripheralworks.client.FlexibleStatueUnbakedModel
import site.siredvin.peripheralworks.common.block.FlexibleRealityAnchor
import site.siredvin.peripheralworks.common.block.FlexibleStatue
import site.siredvin.peripheralworks.common.setup.Blocks

object FabricPeripheralWorksClient : ClientModInitializer {
    override fun onInitializeClient() {
        PeripheralWorksClientCore.onInit()
        ModelLoadingPlugin.register {
            it.addModels(PeripheralWorksClientCore.EXTRA_MODELS.map { id -> ResourceLocation(PeripheralWorksCore.MOD_ID, id) })
            it.resolveModel().register(
                ModelResolver { ctx ->
                    if (ctx.id() == FlexibleRealityAnchor.BLOCK_MODEL_ID || ctx.id() == FlexibleRealityAnchor.ITEM_MODEL_ID) {
                        return@ModelResolver FlexibleRealityAnchorUnbakedModel
                    }
                    if (ctx.id() == FlexibleStatue.BLOCK_MODEL_ID || ctx.id() == FlexibleStatue.ITEM_MODEL_ID) {
                        return@ModelResolver FlexibleStatueUnbakedModel
                    }
                    return@ModelResolver null
                },
            )
        }
        PeripheralWorksClientCore.EXTRA_BLOCK_ENTITY_RENDERERS.forEach {
            BlockEntityRenderers.register(it.get(), PeripheralWorksClientCore.getBlockEntityRendererProvider(it.get()))
        }

        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.FLEXIBLE_REALITY_ANCHOR.get(), RenderType.translucent())
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.FLEXIBLE_STATUE.get(), RenderType.translucent())
    }
}
