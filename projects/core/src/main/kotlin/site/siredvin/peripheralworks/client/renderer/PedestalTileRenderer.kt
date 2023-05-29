package site.siredvin.peripheralworks.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import org.joml.Quaternionf
import org.joml.Vector3d
import site.siredvin.peripheralworks.api.IItemStackHolder
import site.siredvin.peripheralworks.common.block.BasePedestal.Companion.FACING
import java.util.*

class PedestalTileRenderer<T> : BlockEntityRenderer<T> where T : BlockEntity, T : IItemStackHolder {
    fun getItemTranslate(direction: Direction): Vector3d {
        return when (direction) {
            Direction.DOWN -> ITEM_TRANSLATE_DOWN
            Direction.NORTH -> ITEM_TRANSLATE_NORTH
            Direction.SOUTH -> ITEM_TRANSLATE_SOUTH
            Direction.EAST -> ITEM_TRANSLATE_EAST
            Direction.WEST -> ITEM_TRANSLATE_WEST
            else -> ITEM_TRANSLATE_UP
        }
    }

    fun getLabelTranslate(direction: Direction): Vector3d {
        return when (direction) {
            Direction.DOWN -> LABEL_TRANSLATE_DOWN
            Direction.NORTH -> LABEL_TRANSLATE_NORTH
            Direction.SOUTH -> LABEL_TRANSLATE_SOUTH
            Direction.EAST -> LABEL_TRANSLATE_EAST
            Direction.WEST -> LABEL_TRANSLATE_WEST
            Direction.UP -> LABEL_TRANSLATE_UP
        }
    }

    fun itemRotation(direction: Direction): Quaternionf? {
        return when (direction) {
            Direction.NORTH -> Axis.YP.rotationDegrees(90f)
            Direction.SOUTH -> Axis.YP.rotationDegrees(270f)
            Direction.WEST -> Axis.ZP.rotationDegrees(90f)
            Direction.DOWN -> Axis.XP.rotationDegrees(180f)
            else -> null
        }
    }

    fun itemTimeRotation(direction: Direction, time: Float): Quaternionf {
        return if (direction == Direction.WEST) {
            Axis.XP.rotationDegrees(time % 360)
        } else {
            Axis.YP.rotationDegrees(
                time % 360,
            )
        }
    }

    override fun render(
        blockEntity: T,
        f: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int,
    ) {
        val storedStack = blockEntity!!.storedStack
        if (storedStack.isEmpty) return
        val blockDirection = blockEntity.blockState.getValue(FACING)
        if (blockEntity.renderItem) {
            renderItem(storedStack, blockDirection, poseStack, buffer, packedOverlay, packedLight, 0.8f)
        }
        if (blockEntity.renderLabel) {
            renderLabel(
                poseStack,
                buffer,
                packedLight,
                getLabelTranslate(blockDirection),
                storedStack.hoverName,
                0xffffff,
            )
        }
    }

    private fun renderItem(
        stack: ItemStack,
        direction: Direction,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        combinedOverlay: Int,
        lightLevel: Int,
        scale: Float,
    ) {
        val level: Level? = Minecraft.getInstance().level
        Objects.requireNonNull(level)
        val time = (level!!.gameTime * 5).toFloat()
        val translation = getItemTranslate(direction)
        val itemRotation = itemRotation(direction)
        poseStack.pushPose()
        poseStack.translate(translation.x, translation.y, translation.z)
        if (itemRotation != null) poseStack.mulPose(itemRotation)
        poseStack.mulPose(itemTimeRotation(direction, time))
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

    private fun renderLabel(
        stack: PoseStack,
        buffer: MultiBufferSource,
        lightLevel: Int,
        translation: Vector3d,
        text: Component,
        color: Int,
    ) {
        val font = Minecraft.getInstance().font
        stack.pushPose()
        val scale = 0.01f
        val opacity = (.4f * 255.0f).toInt() shl 24
        val offset = (-font.width(text) / 2).toFloat()
        val matrix = stack.last().pose()
        stack.translate(translation.x, translation.y, translation.z)
        stack.scale(scale, scale, scale)
        stack.mulPose(Minecraft.getInstance().entityRenderDispatcher.cameraOrientation())
        stack.mulPose(Axis.ZP.rotationDegrees(180f))
        font.drawInBatch(text, offset, 0f, color, false, matrix, buffer, Font.DisplayMode.NORMAL, opacity, lightLevel)
        stack.popPose()
    }

    companion object {
        private val ITEM_TRANSLATE_UP = Vector3d(0.5, 0.8, 0.5)
        val LABEL_TRANSLATE_UP = Vector3d(0.5, 1.2, 0.5)
        private val ITEM_TRANSLATE_DOWN = Vector3d(0.5, 0.2, 0.5)
        private val LABEL_TRANSLATE_DOWN = Vector3d(0.5, -0.1, 0.5)
        private val ITEM_TRANSLATE_NORTH = Vector3d(0.5, 0.5, 0.1)
        private val LABEL_TRANSLATE_NORTH = Vector3d(0.5, 1.0, 0.5)
        private val ITEM_TRANSLATE_SOUTH = Vector3d(0.5, 0.5, 0.9)
        private val LABEL_TRANSLATE_SOUTH = Vector3d(0.5, 1.0, 0.5)
        private val ITEM_TRANSLATE_EAST = Vector3d(0.9, 0.5, 0.5)
        private val LABEL_TRANSLATE_EAST = Vector3d(0.5, 1.0, 0.5)
        private val ITEM_TRANSLATE_WEST = Vector3d(0.05, 0.5, 0.5)
        private val LABEL_TRANSLATE_WEST = Vector3d(0.5, 1.0, 0.5)
    }
}
