package site.siredvin.peripheralworks.client.configurator

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos

interface ConfigurationModeRender {
    fun render(minecraft: Minecraft, source: BlockPos, poseStack: PoseStack, camera: Camera)
}
