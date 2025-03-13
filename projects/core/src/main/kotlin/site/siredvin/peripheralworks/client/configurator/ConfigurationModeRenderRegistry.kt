package site.siredvin.peripheralworks.client.configurator

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.subsystem.configurator.EntityLinkMode
import site.siredvin.peripheralworks.subsystem.configurator.PeripheralProxyMode
import site.siredvin.peripheralworks.subsystem.configurator.RemoteObserverMode

object ConfigurationModeRenderRegistry {
    private val REGISTRY = mutableMapOf<ResourceLocation, ConfigurationModeRender>()

    init {
        register(RemoteObserverMode.modeID, RemoteObserverClientRender)
        register(PeripheralProxyMode.modeID, PeripheralProxyClientRender)
        register(EntityLinkMode.modeID, EntityLinkClientRenderer)
    }

    fun register(modeID: ResourceLocation, render: ConfigurationModeRender) {
        REGISTRY[modeID] = render
    }

    fun get(modeID: ResourceLocation): ConfigurationModeRender? = REGISTRY[modeID]

    fun render(poseStack: PoseStack, bufferSource: MultiBufferSource, level: ClientLevel, camera: Camera) {
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return
        if (player.mainHandItem.`is`(Items.ULTIMATE_CONFIGURATOR.get())) {
            val activeModePair = Items.ULTIMATE_CONFIGURATOR.get().getActiveMode(player.mainHandItem) ?: return
            val renderMode = get(activeModePair.first.modeID) ?: return
            renderMode.render(minecraft, activeModePair.second, poseStack, camera)
        }
    }
}
