package site.siredvin.peripheralworks.client.configurator

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.core.BlockPos
import org.joml.Matrix4f

object EntityLinkClientRenderer: ConfigurationModeRender {
    private val sourceFlareColor = FlareRenderer.FlareColor(0.165f, 0.616f, 0.561f)
    override fun render(
        minecraft: Minecraft,
        source: BlockPos,
        poseStack: PoseStack,
        partialTick: Float,
        camera: Camera,
        gameRenderer: GameRenderer,
        projectionMatrix: Matrix4f
    ) {
        FlareRenderer.initFlareRenderer(poseStack, camera)
        FlareRenderer.renderFlare(
            poseStack,
            camera,
            partialTick,
            source.x + 0.5,
            source.y + 0.5,
            source.z + 0.5,
            sourceFlareColor,
            1f,
        )
        FlareRenderer.uninitFlareRenderer(poseStack)
    }
}