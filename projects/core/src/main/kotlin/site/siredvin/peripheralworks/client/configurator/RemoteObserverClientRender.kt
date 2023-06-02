package site.siredvin.peripheralworks.client.configurator

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.core.BlockPos
import org.joml.Matrix4f
import site.siredvin.peripheralworks.common.blockentity.RemoteObserverBlockEntity

object RemoteObserverClientRender : ConfigurationModeRender {

    private val targetFlareColor = FlareRenderer.FlareColor(0.957f, 0.635f, 0.38f)
    private val sourceFlareColor = FlareRenderer.FlareColor(0.165f, 0.616f, 0.561f)

    override fun render(
        minecraft: Minecraft,
        source: BlockPos,
        poseStack: PoseStack,
        partialTick: Float,
        camera: Camera,
        gameRenderer: GameRenderer,
        projectionMatrix: Matrix4f,
    ) {
        FlareRenderer.initFlareRenderer(poseStack, camera)
        val entity = minecraft.level?.getBlockEntity(source) as? RemoteObserverBlockEntity ?: return
        FlareRenderer.renderFlare(
            poseStack,
            camera,
            partialTick,
            entity.blockPos.x + 0.5,
            entity.blockPos.y + 0.5,
            entity.blockPos.z + 0.5,
            sourceFlareColor,
            1f,
        )
        entity.trackedBlocksView.forEach {
            FlareRenderer.renderFlare(
                poseStack,
                camera,
                partialTick,
                it.x + 0.5,
                it.y + 0.5,
                it.z + 0.5,
                targetFlareColor,
                1f,
            )
        }
        FlareRenderer.uninitFlareRenderer(poseStack)
    }
}
