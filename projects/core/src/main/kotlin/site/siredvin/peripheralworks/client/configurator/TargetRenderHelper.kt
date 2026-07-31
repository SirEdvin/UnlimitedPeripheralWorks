package site.siredvin.peripheralworks.client.configurator

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.math.Axis
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.LightTexture
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import site.siredvin.peripheralworks.subsystem.configurator.BoxStyle
import site.siredvin.peripheralworks.subsystem.configurator.TextStyle

object TargetRenderHelper {
    data class Effect(
        val box: AABB,
        val flare: Vec3,
        val style: BoxStyle,
        val color: Int,
        val face: Direction? = null,
        val faceColor: Int = color,
    )
    data class Label(val text: Component, val pos: Vec3, val style: TextStyle, val color: Int = 0xffffff)

    fun renderEffects(poseStack: PoseStack, camera: Camera, partialTick: Float, effects: List<Effect>) {
        if (effects.any { it.style == BoxStyle.OUTLINE || it.style == BoxStyle.FILLED }) {
            CommonRenderer.initRenderer(poseStack, camera)
            RenderSystem.disableDepthTest()
            RenderSystem.disableCull()
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
            RenderSystem.depthMask(false)
            renderBoxes(poseStack, effects, BoxStyle.OUTLINE)
            renderBoxes(poseStack, effects, BoxStyle.FILLED)
            RenderSystem.enableDepthTest()
            RenderSystem.enableCull()
            RenderSystem.depthMask(true)
            CommonRenderer.uninitRenderer(poseStack)
        }
        if (effects.any { it.style == BoxStyle.FLARE }) {
            FlareRenderer.initRenderer(poseStack, camera)
            effects.filter { it.style == BoxStyle.FLARE }.forEach {
                FlareRenderer.renderFlare(poseStack, camera, partialTick, it.flare.x, it.flare.y, it.flare.z, flareColor(it.color), 1f)
            }
            FlareRenderer.uninitRenderer(poseStack)
        }
    }

    fun renderLabels(poseStack: PoseStack, camera: Camera, labels: List<Label>) {
        if (labels.none { it.style != TextStyle.NONE }) return
        CommonRenderer.initRenderer(poseStack, camera)
        RenderSystem.disableDepthTest()
        RenderSystem.disableCull()
        RenderSystem.depthMask(false)
        val buffer = Minecraft.getInstance().renderBuffers().bufferSource()
        labels.filter { it.style != TextStyle.NONE }.forEach {
            poseStack.pushPose()
            poseStack.translate(it.pos.x, it.pos.y, it.pos.z)
            poseStack.mulPose(camera.rotation())
            poseStack.mulPose(Axis.YP.rotationDegrees(180f))
            poseStack.scale(-0.015f, -0.015f, 0.015f)
            val text = if (it.style == TextStyle.BOLD) it.text.copy().withStyle { style -> style.withBold(true) } else it.text
            val font = Minecraft.getInstance().font
            font.drawInBatch(text, -font.width(text) / 2f, 0f, it.color, false, poseStack.last().pose(), buffer, Font.DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT)
            poseStack.popPose()
        }
        buffer.endBatch()
        RenderSystem.enableDepthTest()
        RenderSystem.enableCull()
        RenderSystem.depthMask(true)
        CommonRenderer.uninitRenderer(poseStack)
    }

    private fun renderBoxes(poseStack: PoseStack, effects: List<Effect>, style: BoxStyle) {
        if (effects.none { it.style == style }) return
        val filled = style == BoxStyle.FILLED
        val buffer = if (filled) {
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()
            RenderSystem.setShader(GameRenderer::getPositionColorShader)
            Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR)
        } else {
            RenderSystem.setShader(GameRenderer::getRendertypeLinesShader)
            RenderSystem.lineWidth(4f)
            Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL)
        }
        effects.filter { it.style == style }.forEach {
            val box = it.box.inflate(0.01)
            renderBox(poseStack, buffer, box, it.color, filled)
            it.face?.let { face -> renderBox(poseStack, buffer, faceBox(box, face), it.faceColor, filled) }
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow())
        if (filled) RenderSystem.disableBlend() else RenderSystem.lineWidth(1f)
    }

    private fun renderBox(poseStack: PoseStack, buffer: VertexConsumer, box: AABB, color: Int, filled: Boolean) {
        val red = (color shr 16 and 0xff) / 255f
        val green = (color shr 8 and 0xff) / 255f
        val blue = (color and 0xff) / 255f
        if (filled) {
            LevelRenderer.addChainedFilledBoxVertices(poseStack, buffer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, 0.45f)
        } else {
            LevelRenderer.renderLineBox(poseStack, buffer, box, red, green, blue, 1f)
        }
    }

    private fun faceBox(box: AABB, face: Direction): AABB = when (face) {
        Direction.DOWN -> AABB(box.minX, box.minY - FACE_DEPTH, box.minZ, box.maxX, box.minY, box.maxZ)
        Direction.UP -> AABB(box.minX, box.maxY, box.minZ, box.maxX, box.maxY + FACE_DEPTH, box.maxZ)
        Direction.NORTH -> AABB(box.minX, box.minY, box.minZ - FACE_DEPTH, box.maxX, box.maxY, box.minZ)
        Direction.SOUTH -> AABB(box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ + FACE_DEPTH)
        Direction.WEST -> AABB(box.minX - FACE_DEPTH, box.minY, box.minZ, box.minX, box.maxY, box.maxZ)
        Direction.EAST -> AABB(box.maxX, box.minY, box.minZ, box.maxX + FACE_DEPTH, box.maxY, box.maxZ)
    }

    private fun flareColor(color: Int) = FlareRenderer.FlareColor(
        (color shr 16 and 0xff) / 255f,
        (color shr 8 and 0xff) / 255f,
        (color and 0xff) / 255f,
    )

    private const val FACE_DEPTH = 0.002
}
