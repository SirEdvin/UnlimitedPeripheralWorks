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
    override fun render(
        minecraft: Minecraft,
        source: BlockPos,
        poseStack: PoseStack,
        partialTick: Float,
        camera: Camera,
        projectionMatrix: Matrix4f,
    ) {
        val entity = minecraft.level?.getBlockEntity(source) as? PeripheralProxyBlockEntity ?: return
        val effects = buildList {
            add(TargetRenderHelper.Effect(AABB(entity.blockPos), Vec3.atCenterOf(entity.blockPos), entity.boxStyle, COLOR))
            entity.remotePeripherals.values.forEach {
                val normal = it.direction.normal
                add(
                    TargetRenderHelper.Effect(
                        AABB(it.targetBlock),
                        Vec3(it.targetBlock.x + 0.5 + 0.45 * normal.x, it.targetBlock.y + 0.5 + 0.45 * normal.y, it.targetBlock.z + 0.5 + 0.45 * normal.z),
                        entity.boxStyle,
                        COLOR,
                    ),
                )
            }
        }
        TargetRenderHelper.renderEffects(poseStack, camera, partialTick, effects)
        TargetRenderHelper.renderLabels(
            poseStack,
            camera,
            entity.remotePeripherals.values.mapNotNull {
                it.peripheralName?.let { name -> TargetRenderHelper.Label(Component.literal(name), Vec3(it.targetBlock.x + 0.5, it.targetBlock.y + 1.2, it.targetBlock.z + 0.5), entity.textStyle) }
            },
        )
    }

    private const val COLOR = 0x2a9d8f
}
