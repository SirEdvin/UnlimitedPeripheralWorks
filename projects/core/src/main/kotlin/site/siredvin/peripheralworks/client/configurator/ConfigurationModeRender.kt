package site.siredvin.peripheralworks.client.configurator

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.core.BlockPos
import org.joml.Matrix4f

interface ConfigurationModeRender {
    fun render(minecraft: Minecraft, source: BlockPos, poseStack: PoseStack, partialTick: Float, camera: Camera, gameRenderer: GameRenderer, projectionMatrix: Matrix4f)
}
