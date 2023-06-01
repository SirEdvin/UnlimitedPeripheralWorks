package site.siredvin.peripheralworks.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import org.joml.Vector3d
import site.siredvin.peripheralworks.common.blockentity.PeripheralProxyBlockEntity
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class PeripheralProxyRenderer: BlockEntityRenderer<PeripheralProxyBlockEntity> {

    companion object {
        const val CIRCLE_RADIUS = 0.4
    }

    private fun renderItem(
        stack: ItemStack,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        combinedOverlay: Int,
        lightLevel: Int,
        startingAngle: Double,
        scale: Float,
    ) {
        val level: Level? = Minecraft.getInstance().level
        Objects.requireNonNull(level)
        val time = (level!!.gameTime * 5).toFloat()
        val shiftedAngle = (startingAngle + time) % 360
        val translation = Vector3d(0.5 + CIRCLE_RADIUS * cos(Math.toRadians(shiftedAngle)), 0.5, 0.5 + CIRCLE_RADIUS * sin(Math.toRadians(shiftedAngle)))
        val itemRotation = Axis.YP.rotationDegrees(90f)
        poseStack.pushPose()
        poseStack.translate(translation.x, translation.y, translation.z)
        poseStack.mulPose(itemRotation)
        poseStack.scale(scale, scale, scale)
        val model = Minecraft.getInstance().itemRenderer.getModel(stack, level, null, lightLevel)
        Minecraft.getInstance().itemRenderer.render(
            stack,
            ItemDisplayContext.GROUND,
            true,
            poseStack,
            buffer,
            lightLevel,
            combinedOverlay,
            model,
        )
        poseStack.popPose()
    }
    override fun render(
        blockEntity: PeripheralProxyBlockEntity,
        partialTick: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int
    ) {
        val angleStep = 360.0 / blockEntity.remotePeripherals.size.toDouble()
        var startingAngle = 0.0
        blockEntity.remotePeripherals.values.forEach {
            if (!it.stack.isEmpty) {
                renderItem(it.stack, poseStack, buffer, packedOverlay, packedLight, startingAngle, 0.8f);
            }
            startingAngle += angleStep
        }
    }
}