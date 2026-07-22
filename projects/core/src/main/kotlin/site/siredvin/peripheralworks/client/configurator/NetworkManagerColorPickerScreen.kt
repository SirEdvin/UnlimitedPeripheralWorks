package site.siredvin.peripheralworks.client.configurator

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractSliderButton
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import site.siredvin.peripheralworks.data.ModText
import kotlin.math.roundToInt

class NetworkManagerColorPickerScreen(
    private val parent: Screen,
    initialColor: Int,
    private val apply: (Int) -> Unit,
) : Screen(ModText.NETWORK_MANAGER_COLOR_PICKER.text) {
    private var color = initialColor and 0xffffff
    private var updating = false
    private lateinit var hexBox: EditBox
    private lateinit var red: ChannelSlider
    private lateinit var green: ChannelSlider
    private lateinit var blue: ChannelSlider

    override fun init() {
        val left = (width - 240) / 2
        val top = (height - 150) / 2
        hexBox = addRenderableWidget(
            EditBox(font, left, top, 240, 20, ModText.NETWORK_MANAGER_COLOR.text).apply {
                setMaxLength(7)
                setValue("#%06X".format(color))
                setResponder { value ->
                    if (!updating) {
                        value.removePrefix("#").takeIf { it.length == 6 }?.toIntOrNull(16)?.let {
                            color = it
                            updateSliders()
                        }
                    }
                }
            },
        )
        red = addRenderableWidget(ChannelSlider(left, top + 28, "R", 16))
        green = addRenderableWidget(ChannelSlider(left, top + 52, "G", 8))
        blue = addRenderableWidget(ChannelSlider(left, top + 76, "B", 0))
        addRenderableWidget(Button.builder(ModText.NETWORK_MANAGER_APPLY.text) { applyAndClose() }.bounds(left, top + 112, 116, 20).build())
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL) { onClose() }.bounds(left + 124, top + 112, 116, 20).build())
        setInitialFocus(hexBox)
    }

    private fun updateSliders() {
        red.setChannel()
        green.setChannel()
        blue.setChannel()
    }

    private fun updateHex() {
        if (!::hexBox.isInitialized) return
        updating = true
        hexBox.setValue("#%06X".format(color))
        updating = false
    }

    private fun applyAndClose() {
        color = hexBox.value.removePrefix("#").takeIf { it.length == 6 }?.toIntOrNull(16) ?: return
        apply(color)
        minecraft?.setScreen(parent)
    }

    override fun onClose() {
        minecraft?.setScreen(parent)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            applyAndClose()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(graphics)
        super.render(graphics, mouseX, mouseY, partialTick)
        graphics.drawCenteredString(font, title, width / 2, (height - 150) / 2 - 18, 0xffffff)
        val previewLeft = width / 2 - 24
        val previewTop = (height - 150) / 2 + 100
        graphics.fill(previewLeft - 1, previewTop - 1, previewLeft + 49, previewTop + 9, 0xffffffff.toInt())
        graphics.fill(previewLeft, previewTop, previewLeft + 48, previewTop + 8, 0xff000000.toInt() or color)
    }

    private inner class ChannelSlider(x: Int, y: Int, private val label: String, private val shift: Int) : AbstractSliderButton(x, y, 240, 20, CommonComponents.EMPTY, ((color shr shift) and 0xff) / 255.0) {
        init {
            updateMessage()
        }

        override fun updateMessage() {
            message = Component.literal("$label: ${(value * 255).roundToInt()}")
        }

        override fun applyValue() {
            color = (color and (0xff shl shift).inv()) or ((value * 255).roundToInt() shl shift)
            updateHex()
        }

        fun setChannel() {
            value = ((color shr shift) and 0xff) / 255.0
            updateMessage()
        }
    }
}
