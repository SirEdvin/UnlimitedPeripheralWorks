package site.siredvin.peripheralworks.client.configurator

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import site.siredvin.peripheralworks.common.blockentity.PeripheralProxyBlockEntity
import site.siredvin.peripheralworks.common.blockentity.RemoteObserverBlockEntity
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.peripheralworks.networking.ClientNetworking
import site.siredvin.peripheralworks.networking.TargetRenderStyleMessage
import site.siredvin.peripheralworks.subsystem.configurator.BoxStyle
import site.siredvin.peripheralworks.subsystem.configurator.TextStyle

class TargetRenderSettingsScreen(private val pos: BlockPos) : Screen(ModText.TARGET_RENDER_SETTINGS_TITLE.text) {
    private val target: Any?
        get() = minecraft?.level?.getBlockEntity(pos)?.takeIf { it is PeripheralProxyBlockEntity || it is RemoteObserverBlockEntity }

    override fun isPauseScreen(): Boolean = false

    override fun init() {
        if (target == null) return unavailable()
        val width = 180
        val left = (this.width - width) / 2
        addRenderableWidget(RenderStyleButton(left, height / 2 - 24, width, textStyleText()) { cycleTextStyle(it) })
        addRenderableWidget(RenderStyleButton(left, height / 2 + 4, width, boxStyleText()) { cycleBoxStyle(it) })
    }

    private fun textStyle(): TextStyle? = when (val target = target) {
        is PeripheralProxyBlockEntity -> target.textStyle
        is RemoteObserverBlockEntity -> target.textStyle
        else -> null
    }

    private fun boxStyle(): BoxStyle? = when (val target = target) {
        is PeripheralProxyBlockEntity -> target.boxStyle
        is RemoteObserverBlockEntity -> target.boxStyle
        else -> null
    }

    private fun cycleTextStyle(direction: Int) {
        val current = textStyle() ?: return unavailable()
        val style = TextStyle.entries[Math.floorMod(current.ordinal + direction, TextStyle.entries.size)]
        when (val target = target) {
            is PeripheralProxyBlockEntity -> target.setTextStyle(style)
            is RemoteObserverBlockEntity -> target.setTextStyle(style)
        }
        ClientNetworking.sendToServer(TargetRenderStyleMessage(pos, true, style.name.lowercase()))
        rebuild()
    }

    private fun cycleBoxStyle(direction: Int) {
        val current = boxStyle() ?: return unavailable()
        val style = BoxStyle.entries[Math.floorMod(current.ordinal + direction, BoxStyle.entries.size)]
        when (val target = target) {
            is PeripheralProxyBlockEntity -> target.setBoxStyle(style)
            is RemoteObserverBlockEntity -> target.setBoxStyle(style)
        }
        ClientNetworking.sendToServer(TargetRenderStyleMessage(pos, false, style.name.lowercase()))
        rebuild()
    }

    private fun textStyleText(): Component = ModText.NETWORK_MANAGER_TEXT_STYLE.format(
        when (textStyle()) {
            TextStyle.NONE -> ModText.NETWORK_MANAGER_STYLE_NONE.text
            TextStyle.BOLD -> ModText.NETWORK_MANAGER_TEXT_BOLD.text
            else -> ModText.NETWORK_MANAGER_TEXT_REGULAR.text
        },
    )

    private fun boxStyleText(): Component = ModText.NETWORK_MANAGER_BOX_STYLE.format(
        when (boxStyle()) {
            BoxStyle.OUTLINE -> ModText.NETWORK_MANAGER_BOX_OUTLINE.text
            BoxStyle.FILLED -> ModText.NETWORK_MANAGER_BOX_FILLED.text
            BoxStyle.FLARE -> ModText.NETWORK_MANAGER_BOX_FLARE.text
            else -> ModText.NETWORK_MANAGER_STYLE_NONE.text
        },
    )

    private fun rebuild() {
        clearWidgets()
        init()
    }

    private fun unavailable() {
        minecraft?.player?.displayClientMessage(ModText.TARGET_RENDER_SETTINGS_UNAVAILABLE.text, true)
        onClose()
    }

    override fun tick() {
        if (target == null) unavailable()
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(graphics, mouseX, mouseY, partialTick)
        super.render(graphics, mouseX, mouseY, partialTick)
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 52, 0xffffff)
    }
}
