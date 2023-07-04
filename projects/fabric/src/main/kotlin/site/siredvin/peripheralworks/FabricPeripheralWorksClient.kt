package site.siredvin.peripheralworks

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import site.siredvin.peripheralworks.client.ExpansionModelProvider
import site.siredvin.peripheralworks.common.setup.Blocks
import java.util.function.Consumer

object FabricPeripheralWorksClient : ClientModInitializer {
    override fun onInitializeClient() {
        PeripheralWorksClientCore.onInit()
        ModelLoadingRegistry.INSTANCE.registerModelProvider { _: ResourceManager, out: Consumer<ResourceLocation> ->
            PeripheralWorksClientCore.registerExtraModels(out)
        }
        PeripheralWorksClientCore.EXTRA_BLOCK_ENTITY_RENDERERS.forEach {
            BlockEntityRenderers.register(it.get(), PeripheralWorksClientCore.getBlockEntityRendererProvider(it.get()))
        }
        ModelLoadingRegistry.INSTANCE.registerResourceProvider { ExpansionModelProvider }

        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.FLEXIBLE_REALITY_ANCHOR.get(), RenderType.translucent())
    }
}
