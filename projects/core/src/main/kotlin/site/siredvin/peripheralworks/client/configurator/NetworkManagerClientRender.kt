package site.siredvin.peripheralworks.client.configurator

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
import site.siredvin.peripheralworks.subsystem.configurator.BoxStyle
import site.siredvin.peripheralworks.subsystem.configurator.NetworkManagerMode
import site.siredvin.peripheralworks.subsystem.configurator.TextStyle

object NetworkManagerClientRender : ConfigurationModeRender {

    override fun render(
        minecraft: Minecraft,
        source: BlockPos,
        poseStack: PoseStack,
        partialTick: Float,
        camera: Camera,
        projectionMatrix: Matrix4f,
    ) {
        val entity = minecraft.level?.getBlockEntity(source) as? NetworkManagerBlockEntity ?: return
        val playerPos = minecraft.player!!.position()
        val rangeSquared = entity.range.toDouble() * entity.range
        val stack = minecraft.player!!.mainHandItem
        val selected = NetworkManagerMode.getSelectedGroup(stack)
        val selectedPrefix = selected?.takeIf { entity.delimiter.isNotEmpty() }?.plus(entity.delimiter)
        val textStyles = NetworkManagerMode.RenderTarget.entries.map { NetworkManagerMode.getTextStyle(stack, it) }
        val boxStyles = NetworkManagerMode.RenderTarget.entries.map { NetworkManagerMode.getBoxStyle(stack, it) }
        val frustum = Frustum(poseStack.last().pose(), projectionMatrix).apply { prepare(camera.position.x, camera.position.y, camera.position.z) }
        val peripherals = buildList {
            entity.clientBlockCache.forEach { (pos, instructions) ->
                if (pos.distToCenterSqr(playerPos.x, playerPos.y, playerPos.z) >= rangeSquared || !frustum.isVisible(AABB(pos).inflate(1.0))) return@forEach
                val selectedGroups = if (selected == null) emptyList() else instructions.groups.filter { group -> group == selected || selectedPrefix != null && group.startsWith(selectedPrefix) }
                val target = when {
                    selectedGroups.isNotEmpty() -> NetworkManagerMode.RenderTarget.SELECTED
                    instructions.groups.isNotEmpty() -> NetworkManagerMode.RenderTarget.GROUPED
                    else -> NetworkManagerMode.RenderTarget.UNGROUPED
                }
                val textStyle = textStyles[target.ordinal]
                val boxStyle = boxStyles[target.ordinal]
                if (textStyle == TextStyle.NONE && boxStyle == BoxStyle.NONE) return@forEach
                val groups = if (target == NetworkManagerMode.RenderTarget.SELECTED) selectedGroups else instructions.groups
                // ponytail: one stable color avoids overlapping effects for multi-group peripherals.
                val color = groups.firstOrNull()?.let { entity.peripheralGroups[it]?.color }?.takeIf { it >= 0 } ?: 0xffffff
                add(RenderedPeripheral(pos, instructions, groups, textStyle, boxStyle, color))
            }
        }
        if (peripherals.isEmpty()) return

        TargetRenderHelper.renderEffects(
            poseStack,
            camera,
            partialTick,
            peripherals.map { TargetRenderHelper.Effect(AABB(it.pos), Vec3.atCenterOf(it.pos), it.boxStyle, it.color) },
        )
        TargetRenderHelper.renderLabels(
            poseStack,
            camera,
            peripherals.flatMap {
                var height = 1.2
                buildList {
                    add(TargetRenderHelper.Label(Component.literal(it.instructions.peripheralName), Vec3(it.pos.x + 0.5, it.pos.y + height, it.pos.z + 0.5), it.textStyle))
                    it.instructions.extraNames.forEach { name ->
                        height += 0.15
                        add(TargetRenderHelper.Label(Component.literal(name), Vec3(it.pos.x + 0.5, it.pos.y + height, it.pos.z + 0.5), it.textStyle, 0xff0000))
                    }
                    it.groups.forEach { group ->
                        height += 0.15
                        val color = entity.peripheralGroups[group]?.color?.takeIf { color -> color >= 0 } ?: 0xffffff
                        add(TargetRenderHelper.Label(Component.literal("group:$group"), Vec3(it.pos.x + 0.5, it.pos.y + height, it.pos.z + 0.5), it.textStyle, color))
                    }
                }
            },
        )
    }

    private data class RenderedPeripheral(
        val pos: BlockPos,
        val instructions: NetworkManagerBlockEntity.DrawingInstructions,
        val groups: List<String>,
        val textStyle: TextStyle,
        val boxStyle: BoxStyle,
        val color: Int,
    )
}
