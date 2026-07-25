package site.siredvin.peripheralworks.client.configurator

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import site.siredvin.peripheralworks.common.item.UltimateConfigurator
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.peripheralworks.networking.ClientNetworking
import site.siredvin.peripheralworks.networking.ConfiguratorTargetActionMessage
import site.siredvin.peripheralworks.subsystem.configurator.BoxStyle
import site.siredvin.peripheralworks.subsystem.configurator.ConfiguratorTarget
import site.siredvin.peripheralworks.subsystem.configurator.TextStyle

class ConfiguratorTargetHistoryScreen : Screen(ModText.CONFIGURATOR_HISTORY_TITLE.text) {
    private var tab = Tab.TARGETS
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
        addRenderableWidget(
            Button.builder(ModText.CONFIGURATOR_HISTORY_TARGETS.text) {
                tab = Tab.TARGETS
                rebuild()
            }.bounds(left, 26, panelWidth / 2 - 2, 20).build(),
        ).active = tab != Tab.TARGETS
        addRenderableWidget(
            Button.builder(ModText.CONFIGURATOR_SETTINGS_TITLE.text) {
                tab = Tab.SETTINGS
                rebuild()
            }.bounds(left + panelWidth / 2 + 2, 26, panelWidth / 2 - 2, 20).build(),
        ).active = tab != Tab.SETTINGS
        if (tab == Tab.SETTINGS) {
            addSettings(configurator, stack, left, panelWidth)
            snapshot = snapshot()
            return
        }

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

    private fun addSettings(configurator: UltimateConfigurator, stack: net.minecraft.world.item.ItemStack, left: Int, panelWidth: Int) {
        val name = addRenderableWidget(
            EditBox(font, left, 62, panelWidth, 20, ModText.CONFIGURATOR_SETTINGS_NAME.text).apply {
                setHint(ModText.CONFIGURATOR_SETTINGS_NAME.text)
                setMaxLength(ConfiguratorTarget.MAX_NAME_LENGTH)
                setValue(stack.takeIf { it.hasCustomHoverName() }?.hoverName?.string.orEmpty())
            },
        )
        addRenderableWidget(Button.builder(ModText.CONFIGURATOR_HISTORY_APPLY.text) { sendSetting(ConfiguratorTargetActionMessage.Action.CONFIGURATOR_NAME, name.value) }.bounds(left, 88, panelWidth, 20).build())
        addRenderableWidget(
            RenderStyleButton(left, 116, panelWidth, textStyleText(configurator.getFavoriteTextStyle(stack))) { direction ->
                val styles = TextStyle.entries
                sendSetting(ConfiguratorTargetActionMessage.Action.DEFAULT_TEXT_STYLE, styles[Math.floorMod(configurator.getFavoriteTextStyle(stack).ordinal + direction, styles.size)].name)
            },
        )
        addRenderableWidget(
            RenderStyleButton(left, 142, panelWidth, boxStyleText(configurator.getFavoriteBoxStyle(stack))) { direction ->
                val styles = BoxStyle.entries
                sendSetting(ConfiguratorTargetActionMessage.Action.DEFAULT_BOX_STYLE, styles[Math.floorMod(configurator.getFavoriteBoxStyle(stack).ordinal + direction, styles.size)].name)
            },
        )
    }

    private fun addRow(target: ConfiguratorTarget, favorite: Boolean, left: Int, top: Int, panelWidth: Int) {
        if (favorite) {
            val stack = minecraft?.player?.mainHandItem ?: return
            val configurator = stack.item as UltimateConfigurator
            addTargetButton(target, left, top, panelWidth - 64, target.textColor ?: UltimateConfigurator.DEFAULT_FAVORITE_TEXT_COLOR, target.boxColor ?: UltimateConfigurator.DEFAULT_FAVORITE_BOX_COLOR)
            addRenderableWidget(Button.builder(ModText.CONFIGURATOR_HISTORY_EDIT.text) { minecraft?.setScreen(ConfiguratorFavoriteEditScreen(this, target)) }.bounds(left + panelWidth - 60, top, 60, 20).build())
        } else {
            addTargetButton(target, left, top, panelWidth - 92, 0xffffff, null)
            addRenderableWidget(Button.builder(ModText.CONFIGURATOR_HISTORY_FAVORITE.text) { toggle(target) }.bounds(left + panelWidth - 88, top, 88, 20).build())
        }
    }

    private fun addTargetButton(target: ConfiguratorTarget, left: Int, top: Int, width: Int, textColor: Int, boxColor: Int?) {
        val label = targetLabel(target)
        addRenderableWidget(LeftAlignedButton(left, top, width, label, textColor, boxColor) { send(ConfiguratorTargetActionMessage.Action.SELECT, target) }).tooltip = Tooltip.create(defaultTargetLabel(target))
    }

    private fun targetLabel(target: ConfiguratorTarget): Component = target.name?.let(Component::literal) ?: defaultTargetLabel(target)

    private fun defaultTargetLabel(target: ConfiguratorTarget): Component = Component.translatable("block.${target.modeID.namespace}.${target.modeID.path}")
        .append(" | ${target.dimensionID} | ${target.pos.x}, ${target.pos.y}, ${target.pos.z}")

    private fun toggle(target: ConfiguratorTarget) = send(ConfiguratorTargetActionMessage.Action.TOGGLE_FAVORITE, target)

    private fun send(action: ConfiguratorTargetActionMessage.Action, target: ConfiguratorTarget, name: String = "") {
        ClientNetworking.sendToServer(ConfiguratorTargetActionMessage(action, target.dimensionID, target.pos, name))
    }

    private fun sendSetting(action: ConfiguratorTargetActionMessage.Action, name: String = "", color: Int = -1) {
        ClientNetworking.sendToServer(ConfiguratorTargetActionMessage(action, net.minecraft.resources.ResourceLocation.tryParse("minecraft:overworld")!!, net.minecraft.core.BlockPos.ZERO, name, color))
    }

    private fun textStyleText(style: TextStyle): Component = ModText.NETWORK_MANAGER_TEXT_STYLE.format(
        when (style) {
            TextStyle.NONE -> ModText.NETWORK_MANAGER_STYLE_NONE.text
            TextStyle.REGULAR -> ModText.NETWORK_MANAGER_TEXT_REGULAR.text
            TextStyle.BOLD -> ModText.NETWORK_MANAGER_TEXT_BOLD.text
        },
    )

    private fun boxStyleText(style: BoxStyle): Component = ModText.NETWORK_MANAGER_BOX_STYLE.format(
        when (style) {
            BoxStyle.NONE -> ModText.NETWORK_MANAGER_STYLE_NONE.text
            BoxStyle.OUTLINE -> ModText.NETWORK_MANAGER_BOX_OUTLINE.text
            BoxStyle.FILLED -> ModText.NETWORK_MANAGER_BOX_FILLED.text
            BoxStyle.FLARE -> ModText.NETWORK_MANAGER_BOX_FLARE.text
        },
    )

    private fun configurator(): UltimateConfigurator? {
        val stack = minecraft?.player?.mainHandItem ?: return null
        return (stack.item as? UltimateConfigurator)?.takeIf { stack.`is`(Items.ULTIMATE_CONFIGURATOR.get()) && it.getActiveMode(stack) == null }
    }

    private fun snapshot(): String = minecraft?.player?.mainHandItem?.tag?.toString().orEmpty()

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
        if (tab == Tab.TARGETS && empty) graphics.drawString(font, ModText.CONFIGURATOR_HISTORY_EMPTY.text, left, 56, 0xaaaaaa)
    }

    companion object {
        private const val TARGETS_TOP = 52
    }

    private enum class Tab { TARGETS, SETTINGS }

    private class LeftAlignedButton(x: Int, y: Int, width: Int, message: Component, private val textColor: Int, private val boxColor: Int?, onPress: () -> Unit) : Button(x, y, width, 20, message, { onPress() }, DEFAULT_NARRATION) {
        override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick)
            boxColor?.let { graphics.renderOutline(x, y, width, height, 0xff000000.toInt() or it) }
        }

        override fun renderString(graphics: GuiGraphics, font: Font, color: Int) {
            graphics.enableScissor(x + 4, y, x + width - 4, y + height)
            graphics.drawString(font, message, x + 4, y + (height - 8) / 2, textColor)
            graphics.disableScissor()
        }
    }
}
