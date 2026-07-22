package site.siredvin.peripheralworks.client.configurator

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.AABB
import org.joml.Matrix4f
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
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
        val component = Component.literal(text).withStyle { it.withBold(bold) }
        val offset = (-font.width(component) / 2).toFloat()
        font.drawInBatch(
            component, offset, 0f, color, false, matrix4f, buffer, Font.DisplayMode.SEE_THROUGH, 0,
            LightTexture.FULL_BRIGHT,
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
        val entity = minecraft.level?.getBlockEntity(source) as? NetworkManagerBlockEntity ?: return
        val playerPos = minecraft.player!!.position()
        val range = entity.range
        val stack = minecraft.player!!.mainHandItem
        val selected = NetworkManagerMode.getSelectedGroup(stack)
        val peripherals = entity.clientBlockCache.entries.mapNotNull { (pos, instructions) ->
            if (sqrt(pos.distToCenterSqr(playerPos.x(), playerPos.y(), playerPos.z())) >= range) return@mapNotNull null
            val selectedGroups = instructions.groups.filter { group -> selected != null && (group == selected || entity.delimiter.isNotEmpty() && group.startsWith(selected + entity.delimiter)) }.sorted()
            val target = when {
                selectedGroups.isNotEmpty() -> NetworkManagerMode.RenderTarget.SELECTED
                instructions.groups.isNotEmpty() -> NetworkManagerMode.RenderTarget.GROUPED
                else -> NetworkManagerMode.RenderTarget.UNGROUPED
            }
            val groups = if (target == NetworkManagerMode.RenderTarget.SELECTED) selectedGroups else instructions.groups.sorted()
            // ponytail: one stable color avoids overlapping effects for multi-group peripherals.
            val color = groups.firstOrNull()?.let { entity.peripheralGroups[it]?.color }?.takeIf { it >= 0 } ?: 0xffffff
            RenderedPeripheral(pos, instructions, groups, NetworkManagerMode.getRenderStyle(stack, target), color)
        }

        CommonRenderer.initRenderer(poseStack, camera)
        RenderSystem.disableDepthTest()
        RenderSystem.disableCull()
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)
        RenderSystem.depthMask(false)
        val buffer = minecraft.renderBuffers().bufferSource()
        peripherals.forEach {
            when (it.style) {
                NetworkManagerMode.RenderStyle.TEXT, NetworkManagerMode.RenderStyle.BOLD_TEXT -> {
                    val bold = it.style == NetworkManagerMode.RenderStyle.BOLD_TEXT
                    var baseHeight = 1.2
                    renderText(
                        poseStack,
                        it.instructions.peripheralName,
                        it.pos.x + 0.5,
                        it.pos.y + baseHeight,
                        it.pos.z + 0.5,
                        buffer,
                        bold = bold,
                    )
                    for (extraName in it.instructions.extraNames) {
                        baseHeight += 0.15
                        renderText(
                            poseStack,
                            extraName,
                            it.pos.x + 0.5,
                            it.pos.y + baseHeight,
                            it.pos.z + 0.5,
                            buffer,
                            0xff0000,
                            bold,
                        )
                    }
                    for (group in it.groups) {
                        baseHeight += 0.15
                        renderText(
                            poseStack,
                            "group:$group",
                            it.pos.x + 0.5,
                            it.pos.y + baseHeight,
                            it.pos.z + 0.5,
                            buffer,
                            bold = bold,
                        )
                    }
                }
                NetworkManagerMode.RenderStyle.OUTLINE_BOX -> renderBox(poseStack, buffer, it, false)
                NetworkManagerMode.RenderStyle.FILLED_BOX -> renderBox(poseStack, buffer, it, true)
                else -> Unit
            }
        }
        buffer.endBatch()
        RenderSystem.enableDepthTest()
        RenderSystem.enableCull()
        RenderSystem.depthMask(true)
        CommonRenderer.uninitRenderer(poseStack)

        val flares = peripherals.filter { it.style == NetworkManagerMode.RenderStyle.FLARE }
        if (flares.isNotEmpty()) {
            FlareRenderer.initRenderer(poseStack, camera)
            flares.forEach {
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
    }

    private fun renderBox(poseStack: PoseStack, buffer: MultiBufferSource.BufferSource, peripheral: RenderedPeripheral, filled: Boolean) {
        val red = (peripheral.color shr 16 and 0xff) / 255f
        val green = (peripheral.color shr 8 and 0xff) / 255f
        val blue = (peripheral.color and 0xff) / 255f
        val box = AABB(peripheral.pos).inflate(0.002)
        if (filled) {
            LevelRenderer.addChainedFilledBoxVertices(poseStack, buffer.getBuffer(RenderType.debugFilledBox()), box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, 0.25f)
        } else {
            LevelRenderer.renderLineBox(poseStack, buffer.getBuffer(RenderType.lines()), box, red, green, blue, 1f)
        }
    }

    private data class RenderedPeripheral(
        val pos: BlockPos,
        val instructions: NetworkManagerBlockEntity.DrawingInstructions,
        val groups: List<String>,
        val style: NetworkManagerMode.RenderStyle,
        val color: Int,
    )
}
