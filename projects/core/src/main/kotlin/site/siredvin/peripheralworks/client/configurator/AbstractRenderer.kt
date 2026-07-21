package site.siredvin.peripheralworks.client.configurator

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera

open class AbstractRenderer {
    open fun initRenderer(matrices: PoseStack, camera: Camera) {
//        RenderSystem.disableDepthTest()
//        RenderSystem.disableCull()
//        RenderSystem.enableBlend()
//        RenderSystem.blendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ZERO)
//
        matrices.pushPose()

        matrices.translate(-camera.position.x, -camera.position.y, -camera.position.z)

//        RenderSystem.setShader(GameRenderer::getPositionTexShader)
//        RenderSystem.setShaderTexture(0, flareTexture)
    }

    open fun uninitRenderer(matrices: PoseStack) {
        matrices.popPose()

//        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
//        RenderSystem.defaultBlendFunc()
//        RenderSystem.disableBlend()
//        RenderSystem.enableDepthTest()
    }
}
