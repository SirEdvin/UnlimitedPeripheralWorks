package site.siredvin.peripheralworks.client.configurator

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import site.siredvin.peripheralworks.common.item.UltimateConfigurator
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.peripheralworks.networking.ClientNetworking
import site.siredvin.peripheralworks.networking.ConfiguratorTargetActionMessage
import site.siredvin.peripheralworks.subsystem.configurator.ConfiguratorTarget

class ConfiguratorTargetHistoryScreen : Screen(ModText.CONFIGURATOR_HISTORY_TITLE.text) {
    private var page = 0
    private var snapshot = ""
    private var empty = false

    override fun isPauseScreen(): Boolean = false

    override fun init() {
        val configurator = configurator() ?: return onClose()
        val stack = minecraft?.player?.mainHandItem ?: return onClose()
        val targets = configurator.getTargetHistory(stack)
        val favorites = configurator.getFavoriteTargets(stack)
        val panelWidth = (width - 24).coerceAtMost(420)
        val left = (width - panelWidth) / 2

        empty = targets.isEmpty()
        val targetsPerPage = ((height - TARGETS_TOP - 24) / 22).coerceIn(3, UltimateConfigurator.MAX_TARGET_HISTORY)
        val pageCount = ((targets.size + targetsPerPage - 1) / targetsPerPage).coerceAtLeast(1)
        page = page.coerceIn(0, pageCount - 1)
        targets.drop(page * targetsPerPage).take(targetsPerPage).forEachIndexed { index, target ->
            addRow(target, favorites.any(target::matches), left, TARGETS_TOP + index * 22, panelWidth)
        }
        if (pageCount > 1) {
            val paginationY = TARGETS_TOP + targetsPerPage * 22
            addRenderableWidget(
                Button.builder(Component.literal("<")) {
                    page--
                    rebuild()
                }.bounds(left, paginationY, 24, 20).build(),
            ).active = page > 0
            addRenderableWidget(Button.builder(Component.literal("${page + 1}/$pageCount")) {}.bounds(left + 28, paginationY, panelWidth - 56, 20).build()).active = false
            addRenderableWidget(
                Button.builder(Component.literal(">")) {
                    page++
                    rebuild()
                }.bounds(left + panelWidth - 24, paginationY, 24, 20).build(),
            ).active = page + 1 < pageCount
        }
        snapshot = snapshot()
    }

    private fun addRow(target: ConfiguratorTarget, favorite: Boolean, left: Int, top: Int, panelWidth: Int) {
        if (favorite) {
            addTargetButton(target, left, top, panelWidth - 64, target.name != null)
            addRenderableWidget(Button.builder(ModText.CONFIGURATOR_HISTORY_EDIT.text) { minecraft?.setScreen(ConfiguratorFavoriteEditScreen(this, target)) }.bounds(left + panelWidth - 60, top, 60, 20).build())
        } else {
            addTargetButton(target, left, top, panelWidth - 92, false)
            addRenderableWidget(Button.builder(ModText.CONFIGURATOR_HISTORY_FAVORITE.text) { toggle(target) }.bounds(left + panelWidth - 88, top, 88, 20).build())
        }
    }

    private fun addTargetButton(target: ConfiguratorTarget, left: Int, top: Int, width: Int, highlighted: Boolean) {
        val label = targetLabel(target)
        addRenderableWidget(LeftAlignedButton(left, top, width, label, highlighted) { send(ConfiguratorTargetActionMessage.Action.SELECT, target) }).tooltip = Tooltip.create(defaultTargetLabel(target))
    }

    private fun targetLabel(target: ConfiguratorTarget): Component = target.name?.let(Component::literal) ?: defaultTargetLabel(target)

    private fun defaultTargetLabel(target: ConfiguratorTarget): Component = Component.translatable("block.${target.modeID.namespace}.${target.modeID.path}")
        .append(" | ${target.dimensionID} | ${target.pos.x}, ${target.pos.y}, ${target.pos.z}")

    private fun toggle(target: ConfiguratorTarget) = send(ConfiguratorTargetActionMessage.Action.TOGGLE_FAVORITE, target)

    private fun send(action: ConfiguratorTargetActionMessage.Action, target: ConfiguratorTarget, name: String = "") {
        ClientNetworking.sendToServer(ConfiguratorTargetActionMessage(action, target.dimensionID, target.pos, name))
    }

    private fun configurator(): UltimateConfigurator? {
        val stack = minecraft?.player?.mainHandItem ?: return null
        return (stack.item as? UltimateConfigurator)?.takeIf { stack.`is`(Items.ULTIMATE_CONFIGURATOR.get()) && it.getActiveMode(stack) == null }
    }

    private fun snapshot(): String = minecraft?.player?.mainHandItem?.tag?.let {
        "${it.get(UltimateConfigurator.RECENT_TARGETS)}:${it.get(UltimateConfigurator.FAVORITE_TARGETS)}"
    }.orEmpty()

    private fun rebuild() {
        clearWidgets()
        init()
    }

    override fun tick() {
        if (configurator() == null) return onClose()
        if (snapshot() != snapshot) rebuild()
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(graphics)
        super.render(graphics, mouseX, mouseY, partialTick)
        val panelWidth = (width - 24).coerceAtMost(420)
        val left = (width - panelWidth) / 2
        graphics.drawCenteredString(font, title, width / 2, 12, 0xffffff)
        graphics.drawString(font, ModText.CONFIGURATOR_HISTORY_TARGETS.text, left, 27, 0xffffff)
        if (empty) graphics.drawString(font, ModText.CONFIGURATOR_HISTORY_EMPTY.text, left, 42, 0xaaaaaa)
    }

    companion object {
        private const val TARGETS_TOP = 38
    }

    private class LeftAlignedButton(x: Int, y: Int, width: Int, message: Component, private val highlighted: Boolean, onPress: () -> Unit) : Button(x, y, width, 20, message, { onPress() }, DEFAULT_NARRATION) {
        override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick)
            if (highlighted) graphics.renderOutline(x, y, width, height, 0xffffaa00.toInt())
        }

        override fun renderString(graphics: GuiGraphics, font: Font, color: Int) {
            graphics.enableScissor(x + 4, y, x + width - 4, y + height)
            graphics.drawString(font, message, x + 4, y + (height - 8) / 2, color)
            graphics.disableScissor()
        }
    }
}
