package site.siredvin.peripheralworks.client.configurator

import com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
import com.mojang.blaze3d.platform.GlStateManager.DestFactor.ZERO
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.math.Axis
import net.minecraft.client.Camera
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import org.joml.Matrix4f
import site.siredvin.peripheralworks.PeripheralWorksCore

// Copy of https://github.com/SwitchCraftCC/Plethora-Fabric/blob/91a64b3cf9f428227425e06bbbc8aa6e9a416bee/src/main/java/io/sc3/plethora/gameplay/overlay/FlareOverlayRenderer.kt#L4
object FlareRenderer {
    private val flareTexture = ResourceLocation(PeripheralWorksCore.MOD_ID, "textures/misc/flare.png")

    fun initFlareRenderer(matrices: PoseStack, camera: Camera) {
        RenderSystem.disableDepthTest()
        RenderSystem.disableCull()
        RenderSystem.enableBlend()
        RenderSystem.blendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ZERO)

        matrices.pushPose()

        matrices.translate(-camera.position.x, -camera.position.y, -camera.position.z)

        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        RenderSystem.setShaderTexture(0, flareTexture)
    }

    fun uninitFlareRenderer(matrices: PoseStack) {
        matrices.popPose()

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.defaultBlendFunc()
        RenderSystem.disableBlend()
        RenderSystem.enableDepthTest()
    }

    fun renderFlare(
        matrices: PoseStack, camera: Camera,
        ticks: Float, x: Double, y: Double, z: Double, color: FlareColor, size: Float
    ) {
        matrices.pushPose()

        // Set up the view
        matrices.translate(x, y, z)
        matrices.mulPose(Axis.YP.rotationDegrees(-camera.yRot))
        matrices.mulPose(Axis.XP.rotationDegrees(camera.xRot))

        // The size is function of ticks and the id: ensures slightly different sizes
        val renderSize = size * 0.2f + Mth.sin(ticks / 100.0f + color.offset) / 16.0f

        // Prepare to render
        val tessellator = Tesselator.getInstance()
        val matrix4f = matrices.last().pose()

        // Inner highlight
        RenderSystem.setShaderColor(color.r, color.g, color.b, 0.5f)
        renderQuad(tessellator, matrix4f, renderSize)

        // Outer aura
        RenderSystem.setShaderColor(color.r, color.g, color.b, 0.2f)
        renderQuad(tessellator, matrix4f, renderSize * 2)

        matrices.popPose()
    }

    private fun renderQuad(tessellator: Tesselator, matrix4f: Matrix4f, size: Float) {
        val buffer = tessellator.builder

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
        buffer.vertex(matrix4f, -size, -size, 0f).uv(0f, 1f).endVertex()
        buffer.vertex(matrix4f, -size, +size, 0f).uv(1f, 1f).endVertex()
        buffer.vertex(matrix4f, +size, +size, 0f).uv(1f, 0f).endVertex()
        buffer.vertex(matrix4f, +size, -size, 0f).uv(0f, 0f).endVertex()
        BufferUploader.drawWithShader(buffer.end())
    }
    data class FlareColor(val r: Float, val g: Float, val b: Float, val offset: Float = 0.1f)
}