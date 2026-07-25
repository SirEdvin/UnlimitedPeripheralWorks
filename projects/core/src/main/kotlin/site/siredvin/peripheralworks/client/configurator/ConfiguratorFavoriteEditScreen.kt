package site.siredvin.peripheralworks.client.configurator

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import org.lwjgl.glfw.GLFW
import site.siredvin.peripheralworks.common.item.UltimateConfigurator
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.peripheralworks.networking.ClientNetworking
import site.siredvin.peripheralworks.networking.ConfiguratorTargetActionMessage
import site.siredvin.peripheralworks.subsystem.configurator.ConfiguratorTarget

class ConfiguratorFavoriteEditScreen(
    private val parent: ConfiguratorTargetHistoryScreen,
    private val target: ConfiguratorTarget,
) : Screen(ModText.CONFIGURATOR_HISTORY_EDIT_TITLE.text) {
    private lateinit var name: EditBox

    override fun isPauseScreen(): Boolean = false

    override fun init() {
        val left = width / 2 - 120
        val top = height / 2 - 44
        name = addRenderableWidget(
            EditBox(font, left, top, 240, 20, ModText.CONFIGURATOR_HISTORY_RENAME.text).apply {
                setHint(ModText.CONFIGURATOR_HISTORY_RENAME.text)
                setMaxLength(ConfiguratorTarget.MAX_NAME_LENGTH)
                setValue(target.name.orEmpty())
            },
        )
        addRenderableWidget(Button.builder(ModText.CONFIGURATOR_HISTORY_APPLY.text) { rename() }.bounds(left, top + 28, 116, 20).build())
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL) { onClose() }.bounds(left + 124, top + 28, 116, 20).build())
        addRenderableWidget(Button.builder(ModText.CONFIGURATOR_HISTORY_UNFAVORITE.text) { removeFavorite() }.bounds(left, top + 56, 240, 20).build())
        setInitialFocus(name)
    }

    private fun rename() {
        send(ConfiguratorTargetActionMessage.Action.RENAME, name.value)
        onClose()
    }

    private fun removeFavorite() {
        send(ConfiguratorTargetActionMessage.Action.TOGGLE_FAVORITE)
        onClose()
    }

    private fun send(action: ConfiguratorTargetActionMessage.Action, name: String = "") {
        ClientNetworking.sendToServer(ConfiguratorTargetActionMessage(action, target.dimensionID, target.pos, name))
    }

    override fun tick() {
        val stack = minecraft?.player?.mainHandItem ?: return minecraft?.setScreen(null) ?: Unit
        val configurator = stack.item as? UltimateConfigurator
        if (!stack.`is`(Items.ULTIMATE_CONFIGURATOR.get()) || configurator?.getActiveMode(stack) != null) minecraft?.setScreen(null)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            rename()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun onClose() {
        minecraft?.setScreen(parent)
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(graphics)
        super.render(graphics, mouseX, mouseY, partialTick)
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 68, 0xffffff)
    }
}
