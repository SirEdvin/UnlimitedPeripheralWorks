package site.siredvin.peripheralworks.client.configurator

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.ConfirmScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.peripheralworks.networking.ClientNetworking
import site.siredvin.peripheralworks.networking.NetworkManagerGroupMessage
import site.siredvin.peripheralworks.subsystem.configurator.NetworkManagerMode

class NetworkManagerScreen(private val pos: BlockPos) : Screen(ModText.NETWORK_MANAGER_SCREEN_TITLE.text) {
    private enum class Tab { GROUPS, MEMBERSHIP, SETTINGS }

    private var tab = Tab.GROUPS
    private var selectedName: String? = null
    private var search = ""
    private var membershipSearch = ""
    private var rename = ""
    private var color = ""
    private var delimiter = ""
    private var range = ""
    private var page = 0
    private var status = CommonComponents.EMPTY
    private var snapshot = ""
    private var rebuildRequested = false
    private var focusedField = "search"
    private var settingsLabels = emptyList<Triple<Component, Int, Int>>()
    private lateinit var searchBox: EditBox
    private lateinit var membershipSearchBox: EditBox
    private lateinit var renameBox: EditBox
    private lateinit var colorBox: EditBox
    private lateinit var delimiterBox: EditBox
    private lateinit var rangeBox: EditBox

    private val manager: NetworkManagerBlockEntity?
        get() = minecraft?.level?.getBlockEntity(pos) as? NetworkManagerBlockEntity

    override fun isPauseScreen(): Boolean = false

    override fun init() {
        val manager = manager ?: return unavailable()
        settingsLabels = emptyList()
        val expandedPaths = minecraft?.player?.mainHandItem?.let(NetworkManagerMode::getExpandedGroupPaths).orEmpty()
        delimiter = manager.delimiter
        range = manager.range.toString()
        selectedName = selectedName?.takeIf(manager.peripheralGroups::containsKey)
            ?: minecraft?.player?.mainHandItem?.let(NetworkManagerMode::getSelectedGroup)?.takeIf(manager.peripheralGroups::containsKey)

        val panelWidth = (width - 24).coerceAtMost(420)
        val left = (width - panelWidth) / 2
        val third = (panelWidth - 8) / 3
        addRenderableWidget(Button.builder(ModText.NETWORK_MANAGER_TAB_GROUPS.text) { switchTab(Tab.GROUPS) }.bounds(left, 24, third, 20).build()).active = tab != Tab.GROUPS
        addRenderableWidget(Button.builder(ModText.NETWORK_MANAGER_TAB_MEMBERSHIP.text) { switchTab(Tab.MEMBERSHIP) }.bounds(left + third + 4, 24, third, 20).build()).active = tab != Tab.MEMBERSHIP
        addRenderableWidget(Button.builder(ModText.NETWORK_MANAGER_TAB_SETTINGS.text) { switchTab(Tab.SETTINGS) }.bounds(left + (third + 4) * 2, 24, third, 20).build()).active = tab != Tab.SETTINGS

        when (tab) {
            Tab.GROUPS -> initGroups(manager, expandedPaths, left, panelWidth)
            Tab.MEMBERSHIP -> initMembership(manager, left, panelWidth)
            Tab.SETTINGS -> initSettings(left, panelWidth)
        }
        snapshot = stateSnapshot(manager)
    }

    private fun initGroups(manager: NetworkManagerBlockEntity, expandedPaths: Set<String>, left: Int, panelWidth: Int) {
        searchBox = editBox(left, 50, panelWidth - 64, ModText.NETWORK_MANAGER_SEARCH, search) {
            search = it
            page = 0
            rebuildRequested = true
        }
        addRenderableWidget(Button.builder(ModText.NETWORK_MANAGER_CREATE.text) { create() }.bounds(left + panelWidth - 60, 50, 60, 20).build())
        val hierarchy = NetworkManagerGroupHierarchy.build(manager.peripheralGroups.keys, delimiter)
        val rows = if (search.isNotBlank()) {
            hierarchy.search(search).map { "  ${it.fullName}" to it.fullName }
        } else {
            flatten(hierarchy.roots, expandedPaths)
        }
        val fieldsY = (height - 122).coerceAtLeast(120)
        addPagedRows(rows, left, 76, panelWidth, (((fieldsY - 76) / 22) - 1).coerceAtLeast(1), { value ->
            manager.peripheralGroups[value.removePrefix(NODE_PREFIX)]?.color?.takeIf { it >= 0 } ?: 0xffffff
        }) { value ->
            if (value.startsWith(NODE_PREFIX)) toggleExpansion(value.removePrefix(NODE_PREFIX)) else select(value)
        }

        renameBox = editBox(left, fieldsY, panelWidth - 64, ModText.NETWORK_MANAGER_RENAME, rename) { rename = it }
        addRenderableWidget(Button.builder(ModText.NETWORK_MANAGER_APPLY.text) { renameSelected() }.bounds(left + panelWidth - 60, fieldsY, 60, 20).build()).active = selectedName != null
        colorBox = editBox(left, fieldsY + 24, panelWidth - 88, ModText.NETWORK_MANAGER_COLOR, color) { color = it }
        colorBox.setMaxLength(7)
        addRenderableWidget(PipetteButton(left + panelWidth - 84, fieldsY + 24) { openColorPicker() }).apply {
            active = selectedName != null
            tooltip = Tooltip.create(ModText.NETWORK_MANAGER_COLOR_PICKER.text)
        }
        addRenderableWidget(Button.builder(ModText.NETWORK_MANAGER_APPLY.text) { colorSelected() }.bounds(left + panelWidth - 60, fieldsY + 24, 60, 20).build()).active = selectedName != null
        addRenderableWidget(Button.builder(ModText.NETWORK_MANAGER_DELETE.text) { confirmDelete() }.bounds(left, fieldsY + 48, panelWidth, 20).build()).apply {
            active = selectedName != null
            tooltip = Tooltip.create(ModText.NETWORK_MANAGER_DELETE_TOOLTIP.text)
        }
        setInitialFocus(
            when (focusedField) {
                "rename" -> renameBox
                "color" -> colorBox
                else -> searchBox
            },
        )
    }

    private fun initMembership(manager: NetworkManagerBlockEntity, left: Int, panelWidth: Int) {
        val selected = selectedName
        if (selected == null) {
            status = ModText.NETWORK_MANAGER_SELECT_GROUP.text
            return
        }
        val members = manager.peripheralGroups[selected]?.peripherals.orEmpty()
        membershipSearchBox = editBox(left, 50, panelWidth, ModText.NETWORK_MANAGER_MEMBERSHIP_SEARCH, membershipSearch) {
            membershipSearch = it
            page = 0
            rebuildRequested = true
        }
        val rows = manager.displayPeripherals.keys.map { peripheralType(it) to it }
            .filter { (type, name) -> membershipSearch.isBlank() || type.contains(membershipSearch, true) || name.contains(membershipSearch, true) }
            .sortedWith(compareBy({ it.first }, { it.second }))
            .map { (_, name) -> "${if (name in members) "[x]" else "[ ]"} $name" to name }
        addPagedRows(rows, left, 76, panelWidth, ((height - 106) / 22).coerceAtLeast(1)) { peripheral ->
            val expectedPresent = peripheral in members
            send(NetworkManagerGroupMessage.Operation.MEMBERSHIP, selected, peripheral, present = !expectedPresent, expectedPresent = expectedPresent)
        }
        setInitialFocus(membershipSearchBox)
    }

    private fun initSettings(left: Int, panelWidth: Int) {
        delimiterBox = editBox(left, 52, panelWidth, ModText.NETWORK_MANAGER_DELIMITER, delimiter) { delimiter = it }
        delimiterBox.setMaxLength(NetworkManagerBlockEntity.MAX_DELIMITER_LENGTH)
        rangeBox = editBox(left, 76, panelWidth, ModText.NETWORK_MANAGER_RANGE, range) { range = it }
        val labelWidth = (panelWidth / 3).coerceAtMost(100)
        val styleWidth = (panelWidth - labelWidth - 8) / 2
        settingsLabels = NetworkManagerMode.RenderTarget.entries.mapIndexed { index, target -> Triple(targetText(target).copy().append(":"), left, 106 + index * 24) }
        NetworkManagerMode.RenderTarget.entries.forEachIndexed { index, target ->
            addRenderableWidget(RenderStyleButton(left + labelWidth, 100 + index * 24, styleWidth, textStyleText(target)) { cycleTextStyle(target, it) })
            addRenderableWidget(RenderStyleButton(left + labelWidth + styleWidth + 4, 100 + index * 24, styleWidth, boxStyleText(target)) { cycleBoxStyle(target, it) })
        }
        addRenderableWidget(Button.builder(ModText.NETWORK_MANAGER_SAVE_SETTINGS.text) { saveSettings() }.bounds(left, 172, panelWidth, 20).build())
        setInitialFocus(if (focusedField == "range") rangeBox else delimiterBox)
    }

    private fun editBox(x: Int, y: Int, width: Int, hint: ModText, value: String, responder: (String) -> Unit): EditBox = addRenderableWidget(
        EditBox(font, x, y, width, 20, hint.text).apply {
            setHint(hint.text)
            setMaxLength(NetworkManagerBlockEntity.MAX_GROUP_NAME_LENGTH)
            setValue(value)
            setResponder(responder)
        },
    )

    private fun addPagedRows(rows: List<Pair<String, String>>, left: Int, top: Int, panelWidth: Int, rowsPerPage: Int, color: (String) -> Int = { 0xffffff }, action: (String) -> Unit) {
        val pageCount = ((rows.size + rowsPerPage - 1) / rowsPerPage).coerceAtLeast(1)
        page = page.coerceIn(0, pageCount - 1)
        rows.drop(page * rowsPerPage).take(rowsPerPage).forEachIndexed { index, (label, value) ->
            addRenderableWidget(LeftAlignedButton(left, top + index * 22, panelWidth, Component.literal(label), color(value)) { action(value) })
        }
        if (pageCount > 1) {
            addRenderableWidget(
                Button.builder(net.minecraft.network.chat.Component.literal("<")) {
                    page--
                    rebuild()
                }.bounds(left, top + rowsPerPage * 22, 24, 20).build(),
            ).active = page > 0
            addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.literal("${page + 1}/$pageCount")) {}.bounds(left + 28, top + rowsPerPage * 22, panelWidth - 56, 20).build()).active = false
            addRenderableWidget(
                Button.builder(net.minecraft.network.chat.Component.literal(">")) {
                    page++
                    rebuild()
                }.bounds(left + panelWidth - 24, top + rowsPerPage * 22, 24, 20).build(),
            ).active = page + 1 < pageCount
        }
    }

    private fun flatten(nodes: List<NetworkManagerGroupNode>, expanded: Set<String>, depth: Int = 0): List<Pair<String, String>> = buildList {
        nodes.forEach { node ->
            val path = node.path.joinToString(delimiter)
            val expandable = node.children.isNotEmpty()
            val marker = if (!expandable) {
                "  "
            } else if (path in expanded) {
                "- "
            } else {
                "+ "
            }
            val isExpanded = expandable && path in expanded
            val prefix = "    ".repeat(depth) + if (depth > 0) "|- " else ""
            if (expandable) add(prefix + marker + node.segment to NODE_PREFIX + path)
            if (!expandable || isExpanded) {
                node.group?.let {
                    val groupDepth = depth + if (expandable) 1 else 0
                    val label = "    ".repeat(groupDepth) + if (groupDepth > 0) "|- ${it.fullName}" else it.fullName
                    add(label to it.fullName)
                }
            }
            if (isExpanded) addAll(flatten(node.children, expanded, depth + 1))
        }
    }

    private fun select(name: String) {
        selectedName = name
        rename = name
        color = manager?.peripheralGroups?.get(name)?.color?.takeIf { it >= 0 }?.let { "#%06X".format(it) } ?: "-1"
        send(NetworkManagerGroupMessage.Operation.SELECT, name)
        rebuild()
    }

    private fun create() {
        val name = search
        val manager = manager ?: return unavailable()
        status = when {
            name.isEmpty() || name.length > NetworkManagerBlockEntity.MAX_GROUP_NAME_LENGTH -> ModText.NETWORK_MANAGER_INVALID_NAME.text
            manager.peripheralGroups.containsKey(name) -> ModText.NETWORK_MANAGER_DUPLICATE_NAME.text
            else -> {
                send(NetworkManagerGroupMessage.Operation.CREATE, name)
                ModText.NETWORK_MANAGER_REQUEST_SENT.text
            }
        }
    }

    private fun renameSelected() {
        val selected = selectedName ?: return
        val manager = manager ?: return unavailable()
        status = when {
            rename.isEmpty() || rename.length > NetworkManagerBlockEntity.MAX_GROUP_NAME_LENGTH -> ModText.NETWORK_MANAGER_INVALID_NAME.text
            rename != selected && manager.peripheralGroups.containsKey(rename) -> ModText.NETWORK_MANAGER_DUPLICATE_NAME.text
            rename == selected -> CommonComponents.EMPTY
            else -> {
                send(NetworkManagerGroupMessage.Operation.RENAME, selected, rename)
                ModText.NETWORK_MANAGER_REQUEST_SENT.text
            }
        }
    }

    private fun setSelectedColor(color: Int) {
        val selected = selectedName ?: return
        send(NetworkManagerGroupMessage.Operation.COLOR, selected, color = color)
        status = ModText.NETWORK_MANAGER_REQUEST_SENT.text
    }

    private fun colorSelected() {
        val parsed = parseColor(color)
        if (parsed == null) {
            status = ModText.NETWORK_MANAGER_INVALID_COLOR.text
            return
        }
        setSelectedColor(parsed)
    }

    private fun openColorPicker() {
        if (selectedName == null) return
        val initial = parseColor(color)?.takeIf { it >= 0 } ?: 0xffffff
        minecraft?.setScreen(
            NetworkManagerColorPickerScreen(this, initial) {
                color = "#%06X".format(it)
                setSelectedColor(it)
            },
        )
    }

    private fun cycleTextStyle(target: NetworkManagerMode.RenderTarget, direction: Int) {
        val stack = minecraft?.player?.mainHandItem ?: return
        val styles = NetworkManagerMode.TextStyle.entries
        val style = styles[Math.floorMod(NetworkManagerMode.getTextStyle(stack, target).ordinal + direction, styles.size)]
        NetworkManagerMode.setTextStyle(stack, target, style)
        send(NetworkManagerGroupMessage.Operation.TEXT_STYLE, target.name, color = style.ordinal)
        rebuild()
    }

    private fun cycleBoxStyle(target: NetworkManagerMode.RenderTarget, direction: Int) {
        val stack = minecraft?.player?.mainHandItem ?: return
        val styles = NetworkManagerMode.BoxStyle.entries
        val style = styles[Math.floorMod(NetworkManagerMode.getBoxStyle(stack, target).ordinal + direction, styles.size)]
        NetworkManagerMode.setBoxStyle(stack, target, style)
        send(NetworkManagerGroupMessage.Operation.BOX_STYLE, target.name, color = style.ordinal)
        rebuild()
    }

    private fun targetText(target: NetworkManagerMode.RenderTarget): Component = when (target) {
        NetworkManagerMode.RenderTarget.SELECTED -> ModText.NETWORK_MANAGER_RENDER_SELECTED.text
        NetworkManagerMode.RenderTarget.GROUPED -> ModText.NETWORK_MANAGER_RENDER_GROUPED.text
        NetworkManagerMode.RenderTarget.UNGROUPED -> ModText.NETWORK_MANAGER_RENDER_UNGROUPED.text
    }

    private fun textStyleText(target: NetworkManagerMode.RenderTarget): Component {
        val style = minecraft?.player?.mainHandItem?.let { NetworkManagerMode.getTextStyle(it, target) } ?: NetworkManagerMode.TextStyle.REGULAR
        val styleText = when (style) {
            NetworkManagerMode.TextStyle.NONE -> ModText.NETWORK_MANAGER_STYLE_NONE.text
            NetworkManagerMode.TextStyle.REGULAR -> ModText.NETWORK_MANAGER_TEXT_REGULAR.text
            NetworkManagerMode.TextStyle.BOLD -> ModText.NETWORK_MANAGER_TEXT_BOLD.text
        }
        return ModText.NETWORK_MANAGER_TEXT_STYLE.format(styleText)
    }

    private fun boxStyleText(target: NetworkManagerMode.RenderTarget): Component {
        val style = minecraft?.player?.mainHandItem?.let { NetworkManagerMode.getBoxStyle(it, target) } ?: NetworkManagerMode.BoxStyle.NONE
        val styleText = when (style) {
            NetworkManagerMode.BoxStyle.NONE -> ModText.NETWORK_MANAGER_STYLE_NONE.text
            NetworkManagerMode.BoxStyle.OUTLINE -> ModText.NETWORK_MANAGER_BOX_OUTLINE.text
            NetworkManagerMode.BoxStyle.FILLED -> ModText.NETWORK_MANAGER_BOX_FILLED.text
            NetworkManagerMode.BoxStyle.FLARE -> ModText.NETWORK_MANAGER_BOX_FLARE.text
        }
        return ModText.NETWORK_MANAGER_BOX_STYLE.format(styleText)
    }

    private fun confirmDelete() {
        val selected = selectedName ?: return
        minecraft?.setScreen(
            ConfirmScreen({ confirmed ->
                if (confirmed) {
                    send(NetworkManagerGroupMessage.Operation.DELETE, selected)
                    status = ModText.NETWORK_MANAGER_REQUEST_SENT.text
                }
                minecraft?.setScreen(this)
            }, ModText.NETWORK_MANAGER_DELETE_CONFIRM.format(selected), ModText.NETWORK_MANAGER_DELETE_WARNING.text),
        )
    }

    private fun saveSettings() {
        val parsedRange = range.toIntOrNull()
        if (parsedRange == null || parsedRange !in NetworkManagerBlockEntity.MIN_RANGE..NetworkManagerBlockEntity.MAX_RANGE) {
            status = ModText.NETWORK_MANAGER_INVALID_RANGE.format(NetworkManagerBlockEntity.MIN_RANGE, NetworkManagerBlockEntity.MAX_RANGE)
            return
        }
        send(NetworkManagerGroupMessage.Operation.SETTINGS, "", delimiter, parsedRange)
        status = ModText.NETWORK_MANAGER_REQUEST_SENT.text
    }

    private fun toggleExpansion(path: String) {
        val stack = minecraft?.player?.mainHandItem ?: return
        val expanded = path !in NetworkManagerMode.getExpandedGroupPaths(stack)
        NetworkManagerMode.setGroupExpanded(stack, path, expanded)
        send(NetworkManagerGroupMessage.Operation.EXPANSION, "", path, present = expanded)
        rebuild()
    }

    private fun send(operation: NetworkManagerGroupMessage.Operation, group: String, value: String = "", color: Int = -1, present: Boolean = false, expectedPresent: Boolean = false) {
        ClientNetworking.sendToServer(NetworkManagerGroupMessage(pos, operation, group, value, color, present, expectedPresent))
    }

    private fun switchTab(newTab: Tab) {
        tab = newTab
        page = 0
        rebuild()
    }

    private fun rebuild() {
        rememberFocus()
        clearWidgets()
        init()
    }

    private fun rememberFocus() {
        focusedField = when (tab) {
            Tab.GROUPS -> when {
                !::searchBox.isInitialized -> focusedField
                renameBox.isFocused -> "rename"
                colorBox.isFocused -> "color"
                else -> "search"
            }
            Tab.SETTINGS -> if (::rangeBox.isInitialized && rangeBox.isFocused) "range" else "delimiter"
            Tab.MEMBERSHIP -> "membership"
        }
    }

    override fun resize(minecraft: Minecraft, width: Int, height: Int) {
        rememberFocus()
        super.resize(minecraft, width, height)
    }

    private fun unavailable() {
        minecraft?.player?.displayClientMessage(ModText.NETWORK_MANAGER_UNAVAILABLE.text, true)
        onClose()
    }

    private fun stateSnapshot(manager: NetworkManagerBlockEntity): String = "${manager.delimiter}:${manager.range}:" + manager.peripheralGroups.toSortedMap().entries.joinToString("|") { (name, group) -> "$name:${group.color}:${group.visibility}:${group.peripherals.sorted()}" } + manager.displayPeripherals.keys.sorted() + minecraft?.player?.mainHandItem?.let(NetworkManagerMode::getSelectedGroup)

    override fun tick() {
        val manager = manager ?: return unavailable()
        val current = stateSnapshot(manager)
        if (rebuildRequested || current != snapshot) {
            rebuildRequested = false
            rebuild()
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            when (tab) {
                Tab.GROUPS -> when {
                    searchBox.isFocused -> create()
                    renameBox.isFocused -> renameSelected()
                    colorBox.isFocused -> colorSelected()
                }
                Tab.SETTINGS -> saveSettings()
                Tab.MEMBERSHIP -> return super.keyPressed(keyCode, scanCode, modifiers)
            }
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(graphics)
        super.render(graphics, mouseX, mouseY, partialTick)
        settingsLabels.forEach { (label, x, y) -> graphics.drawString(font, label, x, y, 0xffffff) }
        graphics.drawCenteredString(font, title, width / 2, 8, 0xffffff)
        selectedName?.let { graphics.drawCenteredString(font, ModText.NETWORK_MANAGER_SELECTED.format(it), width / 2, height - 22, 0xa0ffa0) }
        if (status != CommonComponents.EMPTY) graphics.drawCenteredString(font, status, width / 2, height - 10, 0xffd060)
    }

    companion object {
        private const val NODE_PREFIX = "\u0000"

        private fun parseColor(value: String): Int? = if (value == "-1") -1 else value.removePrefix("#").takeIf { it.length == 6 }?.toIntOrNull(16)

        private fun peripheralType(name: String): String {
            val suffix = name.substringAfterLast('_', "")
            return if (suffix.isNotEmpty() && suffix.all(Char::isDigit)) name.substringBeforeLast('_') else name
        }
    }

    private class LeftAlignedButton(x: Int, y: Int, width: Int, message: Component, private val textColor: Int, onPress: () -> Unit) : Button(x, y, width, 20, message, { onPress() }, DEFAULT_NARRATION) {
        override fun renderString(graphics: GuiGraphics, font: Font, color: Int) {
            graphics.enableScissor(x + 4, y, x + width - 4, y + height)
            graphics.drawString(font, message, x + 4, y + (height - 8) / 2, textColor)
            graphics.disableScissor()
        }
    }

    private class PipetteButton(x: Int, y: Int, onPress: () -> Unit) : Button(x, y, 20, 20, CommonComponents.EMPTY, { onPress() }, DEFAULT_NARRATION) {
        override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick)
            val color = if (active) 0xffffffff.toInt() else 0xff777777.toInt()
            graphics.fill(x + 5, y + 4, x + 9, y + 7, color)
            graphics.fill(x + 8, y + 6, x + 11, y + 10, color)
            graphics.fill(x + 10, y + 9, x + 14, y + 12, color)
            graphics.fill(x + 12, y + 11, x + 15, y + 15, color)
        }
    }

    private class RenderStyleButton(x: Int, y: Int, width: Int, message: Component, private val cycle: (Int) -> Unit) : Button(x, y, width, 20, message, { cycle(1) }, DEFAULT_NARRATION) {
        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            if (button == 1 && active && visible && isMouseOver(mouseX, mouseY)) {
                playDownSound(Minecraft.getInstance().soundManager)
                cycle(-1)
                return true
            }
            return super.mouseClicked(mouseX, mouseY, button)
        }
    }
}
