package site.siredvin.peripheralworks

import net.minecraft.client.Minecraft
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.RenderLevelStageEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import site.siredvin.peripheralworks.client.configurator.ConfigurationModeRenderRegistry

@Mod.EventBusSubscriber(modid = PeripheralWorksCore.MOD_ID, value = [Dist.CLIENT], bus = Mod.EventBusSubscriber.Bus.FORGE)
object ForgePeripheralWorksMainClient {
    @SubscribeEvent
    fun onRender(ev: RenderLevelStageEvent) {
        if (ev.stage == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            ConfigurationModeRenderRegistry.render(Minecraft.getInstance(), ev.poseStack, ev.partialTick, ev.camera, ev.projectionMatrix)
        }
    }
}