package site.siredvin.peripheralworks.client.configurator

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.math.Axis
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.AABB
import org.joml.Matrix4f
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
import site.siredvin.peripheralworks.subsystem.configurator.NetworkManagerMode

object NetworkManagerClientRender : ConfigurationModeRender {

    fun renderText(
        matrices: PoseStack,
        text: String,
        x: Double,
        y: Double,
        z: Double,
        buffer: MultiBufferSource,
        color: Int = 0xffffff,
        bold: Boolean = false,
    ) {
        matrices.pushPose()

        val scaleFactor = 0.015f
        matrices.translate(x, y, z)
        matrices.mulPose(Minecraft.getInstance().entityRenderDispatcher.cameraOrientation())
        matrices.mulPose(Axis.ZP.rotationDegrees(180f))

        matrices.scale(scaleFactor, scaleFactor, scaleFactor)

        val matrix4f = matrices.last().pose()

        val font = Minecraft.getInstance().font
        if (bold) {
            val component = Component.literal(text).withStyle { it.withBold(true) }
            val offset = (-font.width(component) / 2).toFloat()
            font.drawInBatch(component, offset, 0f, color, false, matrix4f, buffer, Font.DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT)
        } else {
            val offset = (-font.width(text) / 2).toFloat()
            font.drawInBatch(text, offset, 0f, color, false, matrix4f, buffer, Font.DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT)
        }

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
                if (textStyle == NetworkManagerMode.TextStyle.NONE && boxStyle == NetworkManagerMode.BoxStyle.NONE) return@forEach
                val groups = if (target == NetworkManagerMode.RenderTarget.SELECTED) selectedGroups else instructions.groups
                // ponytail: one stable color avoids overlapping effects for multi-group peripherals.
                val color = groups.firstOrNull()?.let { entity.peripheralGroups[it]?.color }?.takeIf { it >= 0 } ?: 0xffffff
                add(RenderedPeripheral(pos, instructions, groups, textStyle, boxStyle, color))
            }
        }
        if (peripherals.isEmpty()) return

        if (peripherals.any { it.boxStyle == NetworkManagerMode.BoxStyle.OUTLINE || it.boxStyle == NetworkManagerMode.BoxStyle.FILLED }) {
            CommonRenderer.initRenderer(poseStack, camera)
            RenderSystem.disableDepthTest()
            RenderSystem.disableCull()
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)
            RenderSystem.depthMask(false)
            renderBoxes(poseStack, peripherals, false)
            renderBoxes(poseStack, peripherals, true)
            RenderSystem.enableDepthTest()
            RenderSystem.enableCull()
            RenderSystem.depthMask(true)
            CommonRenderer.uninitRenderer(poseStack)
        }

        if (peripherals.any { it.boxStyle == NetworkManagerMode.BoxStyle.FLARE }) {
            FlareRenderer.initRenderer(poseStack, camera)
            peripherals.forEach {
                if (it.boxStyle != NetworkManagerMode.BoxStyle.FLARE) return@forEach
                val color = it.color
                FlareRenderer.renderFlare(
                    poseStack,
                    camera,
                    partialTick,
                    it.pos.x + 0.5,
                    it.pos.y + 0.5,
                    it.pos.z + 0.5,
                    FlareRenderer.FlareColor((color shr 16 and 0xff) / 255f, (color shr 8 and 0xff) / 255f, (color and 0xff) / 255f),
                    1f,
                )
            }
            FlareRenderer.uninitRenderer(poseStack)
        }

        if (peripherals.any { it.textStyle != NetworkManagerMode.TextStyle.NONE }) {
            CommonRenderer.initRenderer(poseStack, camera)
            RenderSystem.disableDepthTest()
            RenderSystem.disableCull()
            RenderSystem.depthMask(false)
            val buffer = minecraft.renderBuffers().bufferSource()
            peripherals.forEach {
                if (it.textStyle == NetworkManagerMode.TextStyle.NONE) return@forEach
                val bold = it.textStyle == NetworkManagerMode.TextStyle.BOLD
                var baseHeight = 1.2
                renderText(poseStack, it.instructions.peripheralName, it.pos.x + 0.5, it.pos.y + baseHeight, it.pos.z + 0.5, buffer, bold = bold)
                for (extraName in it.instructions.extraNames) {
                    baseHeight += 0.15
                    renderText(poseStack, extraName, it.pos.x + 0.5, it.pos.y + baseHeight, it.pos.z + 0.5, buffer, 0xff0000, bold)
                }
                for (group in it.groups) {
                    baseHeight += 0.15
                    val groupColor = entity.peripheralGroups[group]?.color?.takeIf { color -> color >= 0 } ?: 0xffffff
                    renderText(poseStack, "group:$group", it.pos.x + 0.5, it.pos.y + baseHeight, it.pos.z + 0.5, buffer, groupColor, bold)
                }
            }
            buffer.endBatch()
            RenderSystem.enableDepthTest()
            RenderSystem.enableCull()
            RenderSystem.depthMask(true)
            CommonRenderer.uninitRenderer(poseStack)
        }
    }

    private fun renderBoxes(poseStack: PoseStack, peripherals: List<RenderedPeripheral>, filled: Boolean) {
        val style = if (filled) NetworkManagerMode.BoxStyle.FILLED else NetworkManagerMode.BoxStyle.OUTLINE
        if (peripherals.none { it.boxStyle == style }) return
        val buffer = Tesselator.getInstance().builder
        if (filled) {
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()
            RenderSystem.setShader(GameRenderer::getPositionColorShader)
            buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR)
        } else {
            RenderSystem.setShader(GameRenderer::getRendertypeLinesShader)
            RenderSystem.lineWidth(4f)
            buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL)
        }
        peripherals.forEach {
            if (it.boxStyle != style) return@forEach
            val red = (it.color shr 16 and 0xff) / 255f
            val green = (it.color shr 8 and 0xff) / 255f
            val blue = (it.color and 0xff) / 255f
            val box = AABB(it.pos).inflate(0.01)
            if (filled) {
                LevelRenderer.addChainedFilledBoxVertices(poseStack, buffer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, 0.45f)
            } else {
                LevelRenderer.renderLineBox(poseStack, buffer, box, red, green, blue, 1f)
            }
        }
        BufferUploader.drawWithShader(buffer.end())
        if (filled) {
            RenderSystem.disableBlend()
        } else {
            RenderSystem.lineWidth(1f)
        }
    }

    private data class RenderedPeripheral(
        val pos: BlockPos,
        val instructions: NetworkManagerBlockEntity.DrawingInstructions,
        val groups: List<String>,
        val textStyle: NetworkManagerMode.TextStyle,
        val boxStyle: NetworkManagerMode.BoxStyle,
        val color: Int,
    )
}
