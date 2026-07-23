package site.siredvin.peripheralworks.client.configurator

import com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
import com.mojang.blaze3d.platform.GlStateManager.DestFactor.ZERO
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA
import com.mojang.blaze3d.systems.RenderSystem
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
object FlareRenderer : AbstractRenderer() {
    @Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
    val flareTexture = ResourceLocation(PeripheralWorksCore.MOD_ID, "textures/misc/flare.png")

    override fun initRenderer(matrices: PoseStack, camera: Camera) {
        RenderSystem.disableDepthTest()
        RenderSystem.disableCull()
        RenderSystem.enableBlend()
        RenderSystem.blendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ZERO)

        super.initRenderer(matrices, camera)

        RenderSystem.setShader(GameRenderer::getPositionColorTexShader)
        RenderSystem.setShaderTexture(0, flareTexture)
        Tesselator.getInstance().builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX)
    }

    override fun uninitRenderer(matrices: PoseStack) {
        Tesselator.getInstance().end()
        super.uninitRenderer(matrices)

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.defaultBlendFunc()
        RenderSystem.disableBlend()
        RenderSystem.enableDepthTest()
    }

    fun renderFlare(
        matrices: PoseStack,
        camera: Camera,
        ticks: Float,
        x: Double,
        y: Double,
        z: Double,
        color: FlareColor,
        size: Float,
    ) {
        matrices.pushPose()

        // Set up the view
        matrices.translate(x, y, z)
        matrices.mulPose(Axis.YP.rotationDegrees(-camera.yRot))
        matrices.mulPose(Axis.XP.rotationDegrees(camera.xRot))

        // The size is function of ticks and the id: ensures slightly different sizes
        val renderSize = size * 0.2f + Mth.sin(ticks / 100.0f + color.offset) / 16.0f

        // Prepare to render
        val matrix4f = matrices.last().pose()

        // Inner highlight
        renderQuad(matrix4f, renderSize, color, 0.5f)

        // Outer aura
        renderQuad(matrix4f, renderSize * 2, color, 0.2f)

        matrices.popPose()
    }

    private fun renderQuad(matrix4f: Matrix4f, size: Float, color: FlareColor, alpha: Float) {
        val buffer = Tesselator.getInstance().builder
        buffer.vertex(matrix4f, -size, -size, 0f).color(color.r, color.g, color.b, alpha).uv(0f, 1f).endVertex()
        buffer.vertex(matrix4f, -size, +size, 0f).color(color.r, color.g, color.b, alpha).uv(1f, 1f).endVertex()
        buffer.vertex(matrix4f, +size, +size, 0f).color(color.r, color.g, color.b, alpha).uv(1f, 0f).endVertex()
        buffer.vertex(matrix4f, +size, -size, 0f).color(color.r, color.g, color.b, alpha).uv(0f, 0f).endVertex()
    }
    data class FlareColor(val r: Float, val g: Float, val b: Float, val offset: Float = 0.1f)
}
