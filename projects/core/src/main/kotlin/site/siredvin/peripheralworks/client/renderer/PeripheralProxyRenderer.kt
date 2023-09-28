package site.siredvin.peripheralworks.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import org.joml.Vector3d
import site.siredvin.peripheralworks.common.block.PeripheralProxy
import site.siredvin.peripheralworks.common.blockentity.PeripheralProxyBlockEntity
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class PeripheralProxyRenderer : BlockEntityRenderer<PeripheralProxyBlockEntity> {

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
        direction: Direction,
    ) {
        val level: Level? = Minecraft.getInstance().level
        Objects.requireNonNull(level)
        val time = (level!!.gameTime * 5).toFloat()
        val shiftedAngle = (startingAngle + time) % 360
        val translation = when (direction) {
            Direction.NORTH -> Vector3d(0.5 + CIRCLE_RADIUS * cos(Math.toRadians(shiftedAngle)), 0.5 + CIRCLE_RADIUS * sin(Math.toRadians(shiftedAngle)), 0.5)
            Direction.SOUTH -> Vector3d(0.5 + CIRCLE_RADIUS * cos(Math.toRadians(shiftedAngle)), 0.5 + CIRCLE_RADIUS * sin(Math.toRadians(shiftedAngle)), 0.5)
            Direction.EAST -> Vector3d(0.5, 0.5 + CIRCLE_RADIUS * cos(Math.toRadians(shiftedAngle)), 0.5 + CIRCLE_RADIUS * sin(Math.toRadians(shiftedAngle)))
            Direction.WEST -> Vector3d(0.5, 0.5 + CIRCLE_RADIUS * cos(Math.toRadians(shiftedAngle)), 0.5 + CIRCLE_RADIUS * sin(Math.toRadians(shiftedAngle)))
            Direction.DOWN -> Vector3d(0.5 + CIRCLE_RADIUS * cos(Math.toRadians(shiftedAngle)), 0.4, 0.5 + CIRCLE_RADIUS * sin(Math.toRadians(shiftedAngle)))
            Direction.UP -> Vector3d(0.5 + CIRCLE_RADIUS * cos(Math.toRadians(shiftedAngle)), 0.5, 0.5 + CIRCLE_RADIUS * sin(Math.toRadians(shiftedAngle)))
            else -> Vector3d(0.5 + CIRCLE_RADIUS * cos(Math.toRadians(shiftedAngle)), 0.5, 0.5 + CIRCLE_RADIUS * sin(Math.toRadians(shiftedAngle)))
        }
        val itemRotation = when (direction) {
            Direction.DOWN -> Axis.YP.rotationDegrees(90f)
            Direction.UP -> Axis.YP.rotationDegrees(90f)
            Direction.NORTH -> Axis.YP.rotationDegrees(180f)
            Direction.SOUTH -> Axis.YP.rotationDegrees(0f)
            Direction.EAST -> Axis.YP.rotationDegrees(90f)
            Direction.WEST -> Axis.YP.rotationDegrees(-90f)
            else -> Axis.YP.rotationDegrees(90f)
        }
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
        packedOverlay: Int,
    ) {
        val angleStep = 360.0 / blockEntity.remotePeripherals.size.toDouble()
        var startingAngle = 0.0
        if (!blockEntity.itemStackCacheBuilt) {
            blockEntity.updateCachedStacks(blockEntity.level!!)
        }
        val direction = blockEntity.blockState.getValue(PeripheralProxy.ORIENTATION)
        blockEntity.remotePeripherals.values.forEach {
            if (!it.stack.isEmpty) {
                renderItem(it.stack, poseStack, buffer, packedOverlay, packedLight, startingAngle, 0.8f, direction)
            }
            startingAngle += angleStep
        }
    }
}
