package site.siredvin.peripheralworks.client.configurator

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.core.BlockPos
import org.joml.Matrix4f
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity

object NetworkManagerClientRender : ConfigurationModeRender {

    class DrawingInstructions(val peripheralName: String) {
        val extraNames = mutableListOf<String>()
        val groups = mutableListOf<String>()
    }

    fun renderText(
        matrices: PoseStack,
        text: String,
        x: Double,
        y: Double,
        z: Double,
        lightLevel: Int,
        buffer: MultiBufferSource,
        color: Int = 0xffffff,
    ) {
        matrices.pushPose()

        val scaleFactor = 0.01f
        matrices.translate(x, y, z)
        matrices.mulPose(Minecraft.getInstance().entityRenderDispatcher.cameraOrientation())
        matrices.mulPose(Axis.ZP.rotationDegrees(180f))

        matrices.scale(scaleFactor, scaleFactor, scaleFactor)

        val matrix4f = matrices.last().pose()

        val font = Minecraft.getInstance().font
        val offset = (-font.width(text) / 2).toFloat()
        val opacity = (.4f * 255.0f).toInt() shl 24
        font.drawInBatch(text, offset, 0f, color, false, matrix4f, buffer, Font.DisplayMode.NORMAL, opacity, lightLevel)

        matrices.popPose()
    }

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
        val entity = minecraft.level?.getBlockEntity(source) as? NetworkManagerBlockEntity ?: return

        entity.clientBlockCache.entries.forEach {
            var baseHeight = 1.2
            renderText(
                poseStack,
                it.value.peripheralName,
                it.key.x + 0.5,
                it.key.y + baseHeight,
                it.key.z + 0.5,
                15728640,
                minecraft.renderBuffers().bufferSource(),
            )
            for (extraName in it.value.extraNames) {
                baseHeight += 0.15
                renderText(
                    poseStack,
                    extraName,
                    it.key.x + 0.5,
                    it.key.y + baseHeight,
                    it.key.z + 0.5,
                    15728640,
                    minecraft.renderBuffers().bufferSource(),
                    0xff0000,
                )
            }
            for (group in it.value.groups) {
                baseHeight += 0.15
                val color = entity.peripheralGroups[group]!!.color
                renderText(
                    poseStack,
                    "group:$group",
                    it.key.x + 0.5,
                    it.key.y + baseHeight,
                    it.key.z + 0.5,
                    15728640,
                    minecraft.renderBuffers().bufferSource(),
                    if (color == -1) 0xffffff else color,
                )
            }
        }

        FlareRenderer.uninitFlareRenderer(poseStack)
    }
}
