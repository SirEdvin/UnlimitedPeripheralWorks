package site.siredvin.peripheralworks.client.turtle

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Transformation
import dan200.computercraft.api.client.TransformedModel
import dan200.computercraft.api.client.turtle.TurtleUpgradeModeller
import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleSide
import org.joml.Quaternionf

class ScaledItemModeller<T: ITurtleUpgrade>(scaleFactor: Float, modelPixelSize: Int = 16): TurtleUpgradeModeller<T> {

    companion object {
        fun buildMatrix(side: TurtleSide, scaleFactor: Float, modelPixelSize: Int): Transformation {
            val shiftFactor = (1 - scaleFactor) / (2 * scaleFactor)
            val stack = PoseStack()
            stack.translate(0.5f, 0.5f, 0.5f)
            stack.mulPose(Quaternionf().rotateLocalY(90f * 0.017453292f))
            stack.translate(-0.5f, -0.5f, -0.5f)
            stack.pushPose()

            if (side == TurtleSide.LEFT) {
                stack.translate(0.0, 0.0, -0.4)
            } else {
                stack.translate(0.0, 0.0, 0.4)
            }
            stack.pushPose()
            stack.scale(scaleFactor, scaleFactor, 1f)
            stack.translate(shiftFactor, shiftFactor, 0f)
            if (modelPixelSize * scaleFactor < 12)
                // So, here we center model around turtle side center, instead of turtle center, because turtle is not cubical
                // And we do this only if model with
                stack.translate(1.5 * scaleFactor / 12.0, 0.0, 0.0)
            return Transformation(stack.last().pose())
        }
    }

    private val leftTransformation = buildMatrix(TurtleSide.LEFT, scaleFactor, modelPixelSize)
    private val rightTransformation = buildMatrix(TurtleSide.RIGHT, scaleFactor, modelPixelSize)

    override fun getModel(
        upgrade: T,
        turtle: ITurtleAccess?,
        side: TurtleSide
    ): TransformedModel {
        return TransformedModel.of(upgrade.craftingItem, if (side == TurtleSide.LEFT) leftTransformation else rightTransformation)
    }
}