package site.siredvin.peripheralworks.client.configurator

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.core.BlockPos
import org.joml.Matrix4f
import site.siredvin.peripheralworks.common.blockentity.PeripheralProxyBlockEntity

object PeripheralProxyClientRender : ConfigurationModeRender {

    private val targetFlareColor = FlareRenderer.FlareColor(0.957f, 0.635f, 0.38f)
    private val sourceFlareColor = FlareRenderer.FlareColor(0.165f, 0.616f, 0.561f)

    fun renderText(
        matrices: PoseStack,
        camera: Camera,
        text: String,
        x: Double,
        y: Double,
        z: Double,
        lightLevel: Int,
        buffer: MultiBufferSource,
    ) {
        matrices.pushPose()

        // Set up the view
        matrices.translate(x, y, z)
        matrices.mulPose(Minecraft.getInstance().entityRenderDispatcher.cameraOrientation())
        matrices.mulPose(Axis.ZP.rotationDegrees(180f))
        matrices.scale(0.025f, 0.025f, 0.025f)

        val matrix4f = matrices.last().pose()

        val font = Minecraft.getInstance().font
        val offset = (-font.width(text) / 2).toFloat()
        font.drawInBatch(text, offset, 0f, 0xffffff, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, lightLevel)

        matrices.popPose()
    }

    override fun render(
        minecraft: Minecraft,
        source: BlockPos,
        poseStack: PoseStack,
        partialTick: Float,
        camera: Camera,
        projectionMatrix: Matrix4f,
    ) {
        val entity = minecraft.level?.getBlockEntity(source) as? PeripheralProxyBlockEntity ?: return
        FlareRenderer.initRenderer(poseStack, camera)
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
        entity.remotePeripherals.values.forEach {
            val normal = it.direction.normal
            FlareRenderer.renderFlare(
                poseStack,
                camera,
                partialTick,
                it.targetBlock.x + 0.5 + 0.45 * normal.x,
                it.targetBlock.y + 0.5 + 0.45 * normal.y,
                it.targetBlock.z + 0.5 + 0.45 * normal.z,
                targetFlareColor,
                1f,
            )
            renderText(
                poseStack,
                camera,
                it.peripheralName ?: "",
                it.targetBlock.x + 0.5,
                it.targetBlock.y + 1.5,
                it.targetBlock.z + 0.5,
                LightTexture.FULL_BRIGHT,
                minecraft.renderBuffers().bufferSource(),
            )
        }
        minecraft.renderBuffers().bufferSource().endBatch()
        FlareRenderer.uninitRenderer(poseStack)
    }
}
