package site.siredvin.peripheralworks.client.configurator

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.core.BlockPos
import org.joml.Matrix4f
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity.GroupVisibility
import site.siredvin.peripheralworks.subsystem.configurator.NetworkManagerMode
import kotlin.math.sqrt

object NetworkManagerClientRender : ConfigurationModeRender {

    fun renderText(
        matrices: PoseStack,
        text: String,
        x: Double,
        y: Double,
        z: Double,
        buffer: MultiBufferSource,
        color: Int = 0xffffff,
    ) {
        matrices.pushPose()

        val scaleFactor = 0.015f
        matrices.translate(x, y, z)
        matrices.mulPose(Minecraft.getInstance().entityRenderDispatcher.cameraOrientation())
        matrices.mulPose(Axis.ZP.rotationDegrees(180f))

        matrices.scale(scaleFactor, scaleFactor, scaleFactor)

        val matrix4f = matrices.last().pose()

        val font = Minecraft.getInstance().font
        val offset = (-font.width(text) / 2).toFloat()
        font.drawInBatch(
            text, offset, 0f, color, false, matrix4f, buffer, Font.DisplayMode.SEE_THROUGH, 0,
            LightTexture.FULL_BRIGHT, true,
        )

        matrices.popPose()
    }

    override fun render(
        minecraft: Minecraft,
        source: BlockPos,
        poseStack: PoseStack,
        partialTick: Float,
        camera: Camera,
        projectionMatrix: Matrix4f,
    ) {
        CommonRenderer.initRenderer(poseStack, camera)
        val entity = minecraft.level?.getBlockEntity(source) as? NetworkManagerBlockEntity ?: return
        val playerPos = minecraft.player!!.position()
        val range = entity.range
        val stack = minecraft.player!!.mainHandItem
        val selected = NetworkManagerMode.getSelectedGroup(stack)
        val mode = NetworkManagerMode.getVisualizationMode(stack)
        RenderSystem.disableDepthTest()
        RenderSystem.disableCull()
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)
        RenderSystem.depthMask(false)
        entity.clientBlockCache.entries.forEach {
            val distance = sqrt(it.key.distToCenterSqr(playerPos.x(), playerPos.y(), playerPos.z()))
            val selectedGroups = it.value.groups.filter { group -> selected != null && (group == selected || entity.delimiter.isNotEmpty() && group.startsWith(selected + entity.delimiter)) }
            val forcedGroups = it.value.groups.filter { group -> entity.peripheralGroups[group]?.visibility == GroupVisibility.SHOW }
            val ungrouped = it.value.groups.isEmpty()
            val visible = forcedGroups.isNotEmpty() || when (mode) {
                NetworkManagerMode.VisualizationMode.ALL -> true
                NetworkManagerMode.VisualizationMode.SELECTED -> selectedGroups.isNotEmpty()
                NetworkManagerMode.VisualizationMode.SELECTED_AND_UNGROUPED -> selectedGroups.isNotEmpty() || ungrouped
                NetworkManagerMode.VisualizationMode.UNGROUPED -> ungrouped
            }
            if (distance < range && visible) {
                var baseHeight = 1.2
                renderText(
                    poseStack,
                    it.value.peripheralName,
                    it.key.x + 0.5,
                    it.key.y + baseHeight,
                    it.key.z + 0.5,
                    minecraft.renderBuffers().bufferSource(),
                )
                for (extraName in it.value.extraNames) {
                    baseHeight += 0.15
                    renderText(
                        poseStack,
                        extraName,
                        it.key.x + 0.5,
                        it.key.y + baseHeight,
                        it.key.z + 0.5,
                        minecraft.renderBuffers().bufferSource(),
                        0xff0000,
                    )
                }
                for (group in it.value.groups.filter { group -> entity.peripheralGroups[group]?.visibility != GroupVisibility.HIDE && (mode == NetworkManagerMode.VisualizationMode.ALL || group in selectedGroups || group in forcedGroups) }) {
                    baseHeight += 0.15
                    val color = entity.peripheralGroups[group]!!.color
                    renderText(
                        poseStack,
                        "group:$group",
                        it.key.x + 0.5,
                        it.key.y + baseHeight,
                        it.key.z + 0.5,
                        minecraft.renderBuffers().bufferSource(),
                        if (color == -1) 0xffffff else color,
                    )
                }
            }
        }
        minecraft.renderBuffers().bufferSource().endBatch()
        RenderSystem.enableDepthTest()
        RenderSystem.enableCull()
        RenderSystem.depthMask(false)
        CommonRenderer.uninitRenderer(poseStack)
    }
}
