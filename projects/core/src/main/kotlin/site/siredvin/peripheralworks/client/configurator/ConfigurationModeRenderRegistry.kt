package site.siredvin.peripheralworks.client.configurator

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import site.siredvin.peripheralworks.common.item.UltimateConfigurator
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.subsystem.configurator.EntityLinkMode
import site.siredvin.peripheralworks.subsystem.configurator.NetworkManagerMode
import site.siredvin.peripheralworks.subsystem.configurator.PeripheralProxyMode
import site.siredvin.peripheralworks.subsystem.configurator.RemoteObserverMode

object ConfigurationModeRenderRegistry {
    private val REGISTRY = mutableMapOf<ResourceLocation, ConfigurationModeRender>()

    init {
        register(RemoteObserverMode.modeID, RemoteObserverClientRender)
        register(PeripheralProxyMode.modeID, PeripheralProxyClientRender)
        register(EntityLinkMode.modeID, EntityLinkClientRenderer)
        register(NetworkManagerMode.modeID, NetworkManagerClientRender)
    }

    fun register(modeID: ResourceLocation, render: ConfigurationModeRender) {
        REGISTRY[modeID] = render
    }

    fun get(modeID: ResourceLocation): ConfigurationModeRender? = REGISTRY[modeID]

    fun render(poseStack: PoseStack, bufferSource: MultiBufferSource, level: ClientLevel, camera: Camera) {
        val minecraft = Minecraft.getInstance()
        val partialTick = minecraft.timer.getGameTimeDeltaPartialTick(true)
        val player = minecraft.player ?: return
        if (player.mainHandItem.`is`(Items.ULTIMATE_CONFIGURATOR.get())) {
            val configurator = Items.ULTIMATE_CONFIGURATOR.get()
            val activeModePair = configurator.getActiveMode(player.mainHandItem)
            if (activeModePair == null) {
                val dimension = minecraft.level?.dimension()?.location() ?: return
                val favorites = configurator.getFavoriteTargets(player.mainHandItem).filter { it.dimensionID == dimension }
                val textStyle = configurator.getFavoriteTextStyle(player.mainHandItem)
                val boxStyle = configurator.getFavoriteBoxStyle(player.mainHandItem)
                TargetRenderHelper.renderEffects(
                    poseStack,
                    camera,
                    partialTick,
                    favorites.map { TargetRenderHelper.Effect(AABB(it.pos), Vec3.atCenterOf(it.pos), boxStyle, it.boxColor ?: UltimateConfigurator.DEFAULT_FAVORITE_BOX_COLOR) },
                )
                TargetRenderHelper.renderLabels(
                    poseStack,
                    camera,
                    favorites.map {
                        TargetRenderHelper.Label(
                            it.name?.let(Component::literal) ?: Component.translatable("block.${it.modeID.namespace}.${it.modeID.path}"),
                            Vec3(it.pos.x + 0.5, it.pos.y + 1.2, it.pos.z + 0.5),
                            textStyle,
                            it.textColor ?: UltimateConfigurator.DEFAULT_FAVORITE_TEXT_COLOR,
                        )
                    },
                )
                return
            }
            val renderMode = get(activeModePair.first.modeID) ?: return
            renderMode.render(minecraft, activeModePair.second, poseStack, partialTick, camera, RenderSystem.getProjectionMatrix())
        }
    }
}
