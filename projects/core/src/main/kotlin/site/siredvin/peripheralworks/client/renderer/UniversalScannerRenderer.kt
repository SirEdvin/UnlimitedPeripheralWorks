package site.siredvin.peripheralworks.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import org.joml.Vector3d
import site.siredvin.peripheralium.computercraft.operations.SphereOperationContext
import site.siredvin.peripheralium.computercraft.peripheral.ability.OperationAbility
import site.siredvin.peripheralworks.common.blockentity.UniversalScannerBlockEntity
import site.siredvin.peripheralworks.computercraft.operations.SphereOperations
import java.time.Instant
import kotlin.math.cos
import kotlin.math.sin

class UniversalScannerRenderer : BlockEntityRenderer<UniversalScannerBlockEntity> {
    companion object {
        const val CIRCLE_RADIUS = 0.45
        val STACK by lazy {
            ItemStack(Items.SPYGLASS)
        }
    }
    private fun renderItem(
        level: Level,
        stack: ItemStack,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        combinedOverlay: Int,
        lightLevel: Int,
        radius: Double,
        scale: Float,
        direction: Direction,
        angle: Double,
    ) {
        val translation = when (direction) {
            Direction.NORTH -> Vector3d(0.5 + radius * cos(Math.toRadians(angle)), 0.5 + radius * sin(Math.toRadians(angle)), 0.5)
            Direction.SOUTH -> Vector3d(0.5 + radius * cos(Math.toRadians(angle)), 0.5 + radius * sin(Math.toRadians(angle)), 0.5)
            Direction.EAST -> Vector3d(0.5, 0.5 + radius * cos(Math.toRadians(angle)), 0.5 + radius * sin(Math.toRadians(angle)))
            Direction.WEST -> Vector3d(0.5, 0.5 + radius * cos(Math.toRadians(angle)), 0.5 + radius * sin(Math.toRadians(angle)))
            Direction.DOWN -> Vector3d(0.5 + radius * cos(Math.toRadians(angle)), 0.4, 0.5 + radius * sin(Math.toRadians(angle)))
            Direction.UP -> Vector3d(0.5 + radius * cos(Math.toRadians(angle)), 0.5, 0.5 + radius * sin(Math.toRadians(angle)))
            else -> Vector3d(0.5 + radius * cos(Math.toRadians(angle)), 0.5, 0.5 + radius * sin(Math.toRadians(angle)))
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
        blockEntity: UniversalScannerBlockEntity,
        partialTick: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int,
    ) {
        val level = blockEntity.level!!
        val time = (level.gameTime * 5).toFloat()
        val shiftedAngle = time % 360

        val cooldown = blockEntity.peripheralSettings.getCompound(OperationAbility.COOLDOWNS_TAG).getLong(SphereOperations.STATIONARY_UNIVERSAL_SCAN.settingsName())
        val now = Instant.now().epochSecond
        val circleExpansion = if (cooldown == 0L || now >= cooldown) {
            0.0
        } else {
            (1.5 * (cooldown - now) / SphereOperations.STATIONARY_UNIVERSAL_SCAN.getCooldown(SphereOperationContext(SphereOperations.STATIONARY_UNIVERSAL_SCAN.maxCostRadius))).coerceAtLeast(0.0)
        }
        for (i in 0..3) {
            renderItem(level, STACK, poseStack, buffer, packedOverlay, packedLight, CIRCLE_RADIUS + circleExpansion, 0.4f, Direction.UP, shiftedAngle + 90.0 * i)
            renderItem(level, STACK, poseStack, buffer, packedOverlay, packedLight, CIRCLE_RADIUS + circleExpansion, 0.4f, Direction.NORTH, shiftedAngle + 90.0 * i + 20.0)
            renderItem(level, STACK, poseStack, buffer, packedOverlay, packedLight, CIRCLE_RADIUS + circleExpansion, 0.4f, Direction.EAST, shiftedAngle + 90.0 * i + 40.0)
        }
    }
}
