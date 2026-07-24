package site.siredvin.peripheralworks.client.configurator

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import site.siredvin.peripheralworks.common.blockentity.RemoteObserverBlockEntity

object RemoteObserverClientRender : ConfigurationModeRender {

    private val sourceFlareColor = FlareRenderer.FlareColor(0.165f, 0.616f, 0.561f)

    override fun render(
        minecraft: Minecraft,
        source: BlockPos,
        poseStack: PoseStack,
        partialTick: Float,
        camera: Camera,
        projectionMatrix: Matrix4f,
    ) {
        val entity = minecraft.level?.getBlockEntity(source) as? RemoteObserverBlockEntity ?: return
        TargetRenderHelper.renderEffects(
            poseStack,
            camera,
            partialTick,
            entity.trackedBlocksView.map { TargetRenderHelper.Effect(AABB(it), Vec3.atCenterOf(it), entity.boxStyle, TARGET_COLOR) },
        )
        FlareRenderer.initRenderer(poseStack, camera)
        FlareRenderer.renderFlare(
            poseStack,
            camera,
            partialTick,
            entity.blockPos.x + 0.5,
            entity.blockPos.y + 0.5,
            entity.blockPos.z + 0.5,
            sourceFlareColor,
            1f,
        )
        FlareRenderer.uninitRenderer(poseStack)
        TargetRenderHelper.renderLabels(
            poseStack,
            camera,
            entity.trackedBlocksView.map {
                TargetRenderHelper.Label(minecraft.level!!.getBlockState(it).block.name, Vec3(it.x + 0.5, it.y + 1.2, it.z + 0.5), entity.textStyle)
            },
        )
    }

    private const val TARGET_COLOR = 0xf4a261
}
