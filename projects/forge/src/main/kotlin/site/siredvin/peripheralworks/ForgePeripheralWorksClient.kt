package site.siredvin.peripheralworks

import dan200.computercraft.api.client.turtle.RegisterTurtleModellersEvent
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.upgrades.UpgradeType
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraft.resources.ResourceLocation
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers
import net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional
import net.neoforged.neoforge.client.event.ModelEvent.RegisterGeometryLoaders
import site.siredvin.peripheralworks.client.geometry.FlexibleRealityAnchorGeometryLoader
import site.siredvin.peripheralworks.client.geometry.FlexibleStatueGeometryLoader
import site.siredvin.peripheralworks.forge.ForgeModClientPlatform

@EventBusSubscriber(modid = PeripheralWorksCore.MOD_ID, value = [Dist.CLIENT])
object ForgePeripheralWorksClient {

    init {
        PeripheralWorksClientCore.configure(ForgeModClientPlatform)
    }

    @SubscribeEvent
    @Suppress("UNUSED_PARAMETER")
    fun onClientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork(PeripheralWorksClientCore::onInit)
    }

    @SubscribeEvent
    fun onRegisterRenderers(event: RegisterRenderers) {
        PeripheralWorksClientCore.EXTRA_BLOCK_ENTITY_RENDERERS.forEach {
            event.registerBlockEntityRenderer(it.get(), PeripheralWorksClientCore.getBlockEntityRendererProvider(it.get()))
        }
        ForgeModClientPlatform.BLOCK_ENTITY_RENDERER_SUPPLIER.forEach {
            it.get().forEach { pair ->
                event.registerBlockEntityRenderer(pair.first, pair.second)
            }
        }
    }

    @SubscribeEvent
    fun registerModels(event: RegisterAdditional) {
        PeripheralWorksClientCore.registerExtraModels { model: ResourceLocation ->
            event.register(ModelResourceLocation.standalone(model))
        }
    }

    @SubscribeEvent
    fun registerTurtleModels(event: RegisterTurtleModellersEvent) {
        PeripheralWorksClientCore.onModelRegister { serializer, model ->
            @Suppress("UNCHECKED_CAST")
            event.register(serializer as UpgradeType<ITurtleUpgrade>, model)
        }
    }

    @SubscribeEvent
    fun registerGeometryLoaders(event: RegisterGeometryLoaders) {
        event.register(
            ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "flexible_reality_anchor"),
            FlexibleRealityAnchorGeometryLoader,
        )
        event.register(
            ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "flexible_statue"),
            FlexibleStatueGeometryLoader,
        )
    }
}
