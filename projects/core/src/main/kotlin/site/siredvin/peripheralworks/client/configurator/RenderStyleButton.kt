package site.siredvin.peripheralworks.client.configurator

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.Component

class RenderStyleButton(x: Int, y: Int, width: Int, message: Component, private val cycle: (Int) -> Unit) : Button(x, y, width, 20, message, { cycle(1) }, DEFAULT_NARRATION) {
    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 1 && active && visible && isMouseOver(mouseX, mouseY)) {
            playDownSound(Minecraft.getInstance().soundManager)
            cycle(-1)
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }
}
