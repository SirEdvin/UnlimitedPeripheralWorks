package site.siredvin.peripheralworks.client.configurator

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import site.siredvin.peripheralworks.common.blockentity.PeripheralProxyBlockEntity

object PeripheralProxyClientRender : ConfigurationModeRender {

    private val sourceFlareColor = FlareRenderer.FlareColor(0.165f, 0.616f, 0.561f)

    override fun render(
        minecraft: Minecraft,
        source: BlockPos,
        poseStack: PoseStack,
        partialTick: Float,
        camera: Camera,
        projectionMatrix: Matrix4f,
    ) {
        val entity = minecraft.level?.getBlockEntity(source) as? PeripheralProxyBlockEntity ?: return
        val effects = entity.remotePeripherals.values.map {
            val normal = it.direction.normal
            TargetRenderHelper.Effect(
                AABB(it.targetBlock),
                Vec3(it.targetBlock.x + 0.5 + 0.45 * normal.x, it.targetBlock.y + 0.5 + 0.45 * normal.y, it.targetBlock.z + 0.5 + 0.45 * normal.z),
                entity.boxStyle,
                TARGET_COLOR,
            )
        }
        TargetRenderHelper.renderEffects(poseStack, camera, partialTick, effects)
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
            entity.remotePeripherals.values.mapNotNull {
                it.peripheralName?.let { name -> TargetRenderHelper.Label(Component.literal(name), Vec3(it.targetBlock.x + 0.5, it.targetBlock.y + 1.2, it.targetBlock.z + 0.5), entity.textStyle) }
            },
        )
    }

    private const val TARGET_COLOR = 0xf4a261
}
