package site.siredvin.peripheralworks

import net.minecraft.client.Minecraft
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RenderLevelStageEvent
import site.siredvin.peripheralworks.client.configurator.ConfigurationModeRenderRegistry

@EventBusSubscriber(modid = PeripheralWorksCore.MOD_ID, value = [Dist.CLIENT])
object ForgePeripheralWorksMainClient {
    @SubscribeEvent
    fun onRender(ev: RenderLevelStageEvent) {
        if (ev.stage == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            ConfigurationModeRenderRegistry.render(
                Minecraft.getInstance(),
                ev.poseStack,
                ev.partialTick.getGameTimeDeltaPartialTick(false),
                ev.camera,
                ev.projectionMatrix,
            )
        }
    }
}
