package site.siredvin.peripheralworks.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import site.siredvin.peripheralworks.common.blockentity.UniversalScannerBlockEntity

class UniversalScannerRenderer : BlockEntityRenderer<UniversalScannerBlockEntity> {
    companion object {
        const val SCALE = 1.2f
        val STACK by lazy {
            ItemStack(Items.SPYGLASS)
        }
    }

    override fun render(
        blockEntity: UniversalScannerBlockEntity,
        partialTick: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int,
    ) {
        val level = blockEntity.level!!
        val time = (level.gameTime * 5).toFloat()
        val rotationAngle = time % 360
        poseStack.pushPose()
        poseStack.translate(0.5, 0.5, 0.5)
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle))
        poseStack.mulPose(Axis.ZP.rotationDegrees(-45f))
        poseStack.scale(SCALE, SCALE, SCALE)
        val model = Minecraft.getInstance().itemRenderer.getModel(STACK, level, null, packedLight)
        Minecraft.getInstance().itemRenderer.render(
            STACK,
            ItemDisplayContext.GROUND,
            true,
            poseStack,
            buffer,
            packedLight,
            packedOverlay,
            model,
        )
        poseStack.popPose()
    }
}
