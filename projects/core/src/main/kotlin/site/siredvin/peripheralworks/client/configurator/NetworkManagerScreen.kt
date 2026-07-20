package site.siredvin.peripheralworks.client.configurator

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.ConfirmScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.CommonComponents
import org.lwjgl.glfw.GLFW
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
import site.siredvin.peripheralworks.common.item.UltimateConfigurator
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.peripheralworks.networking.ClientNetworking
import site.siredvin.peripheralworks.networking.NetworkManagerGroupMessage

class NetworkManagerScreen(private val pos: BlockPos) : Screen(ModText.NETWORK_MANAGER_SCREEN_TITLE.text) {
    private enum class Tab { GROUPS, MEMBERSHIP }

    private var tab = Tab.GROUPS
    private var selectedName: String? = null
    private var search = ""
    private var rename = ""
    private var color = ""
    private var delimiter = ""
    private var range = ""
    private var page = 0
    private var status = CommonComponents.EMPTY
    private var snapshot = ""
    private var rebuildRequested = false
    private var focusedField = "search"
    private lateinit var searchBox: EditBox
    private lateinit var renameBox: EditBox
    private lateinit var colorBox: EditBox
    private lateinit var delimiterBox: EditBox
    private lateinit var rangeBox: EditBox

    private val manager: NetworkManagerBlockEntity?
        get() = minecraft?.level?.getBlockEntity(pos) as? NetworkManagerBlockEntity

    override fun isPauseScreen(): Boolean = false

    override fun init() {
        val level = minecraft?.level ?: return
        val manager = manager ?: return unavailable()
        val settings = NetworkManagerClientSettings.get(level.dimension().location(), pos)
        if (delimiter.isEmpty() && range.isEmpty()) {
            delimiter = settings.delimiter
            range = settings.range.toString()
        }
        selectedName = selectedName?.takeIf(manager.peripheralGroups::containsKey)
            ?: minecraft?.player?.mainHandItem?.let(UltimateConfigurator::getSelectedNetworkGroup)?.takeIf(manager.peripheralGroups::containsKey)

        val panelWidth = (width - 24).coerceAtMost(420)
        val left = (width - panelWidth) / 2
        val half = (panelWidth - 4) / 2
        addRenderableWidget(Button.builder(ModText.NETWORK_MANAGER_TAB_GROUPS.text) { switchTab(Tab.GROUPS) }.bounds(left, 24, half, 20).build()).active = tab != Tab.GROUPS
        addRenderableWidget(Button.builder(ModText.NETWORK_MANAGER_TAB_MEMBERSHIP.text) { switchTab(Tab.MEMBERSHIP) }.bounds(left + half + 4, 24, half, 20).build()).active = tab != Tab.MEMBERSHIP

        if (tab == Tab.GROUPS) initGroups(manager, settings, left, panelWidth) else initMembership(manager, left, panelWidth)
        snapshot = stateSnapshot(manager)
    }

    private fun initGroups(manager: NetworkManagerBlockEntity, settings: NetworkManagerClientSettings.Settings, left: Int, panelWidth: Int) {
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
            flatten(hierarchy.roots, settings.expandedPaths)
        }
        val fieldsY = (height - 116).coerceAtLeast(120)
        addPagedRows(rows, left, 76, panelWidth, (((fieldsY - 76) / 22) - 1).coerceAtLeast(1)) { value ->
            if (value.startsWith(NODE_PREFIX)) toggleExpansion(value.removePrefix(NODE_PREFIX)) else select(value)
        }

        renameBox = editBox(left, fieldsY, panelWidth - 64, ModText.NETWORK_MANAGER_RENAME, rename) { rename = it }
        addRenderableWidget(Button.builder(ModText.NETWORK_MANAGER_APPLY.text) { renameSelected() }.bounds(left + panelWidth - 60, fieldsY, 60, 20).build()).active = selectedName != null
        colorBox = editBox(left, fieldsY + 24, panelWidth - 64, ModText.NETWORK_MANAGER_COLOR, color) { color = it }
        addRenderableWidget(Button.builder(ModText.NETWORK_MANAGER_APPLY.text) { colorSelected() }.bounds(left + panelWidth - 60, fieldsY + 24, 60, 20).build()).active = selectedName != null
        delimiterBox = editBox(left, fieldsY + 48, (panelWidth - 8) / 2, ModText.NETWORK_MANAGER_DELIMITER, delimiter) { delimiter = it }
        delimiterBox.setMaxLength(NetworkManagerClientSettings.MAX_DELIMITER_LENGTH)
        rangeBox = editBox(left + (panelWidth + 8) / 2, fieldsY + 48, (panelWidth - 8) / 2, ModText.NETWORK_MANAGER_RANGE, range) { range = it }
        addRenderableWidget(Button.builder(ModText.NETWORK_MANAGER_SAVE_SETTINGS.text) { saveSettings() }.bounds(left, fieldsY + 72, panelWidth - 64, 20).build())
        addRenderableWidget(Button.builder(ModText.NETWORK_MANAGER_DELETE.text) { confirmDelete() }.bounds(left + panelWidth - 60, fieldsY + 72, 60, 20).build()).apply {
            active = selectedName != null
            tooltip = Tooltip.create(ModText.NETWORK_MANAGER_DELETE_TOOLTIP.text)
        }
        setInitialFocus(
            when (focusedField) {
                "rename" -> renameBox
                "color" -> colorBox
                "delimiter" -> delimiterBox
                "range" -> rangeBox
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
        val rows = manager.displayPeripherals.keys.sorted().map { "${if (it in members) "[x]" else "[ ]"} $it" to it }
        addPagedRows(rows, left, 52, panelWidth, ((height - 82) / 22).coerceAtLeast(1)) { peripheral ->
            val expectedPresent = peripheral in members
            send(NetworkManagerGroupMessage.Operation.MEMBERSHIP, selected, peripheral, present = !expectedPresent, expectedPresent = expectedPresent)
        }
    }

    private fun editBox(x: Int, y: Int, width: Int, hint: ModText, value: String, responder: (String) -> Unit): EditBox = addRenderableWidget(
        EditBox(font, x, y, width, 20, hint.text).apply {
            setHint(hint.text)
            setMaxLength(NetworkManagerBlockEntity.MAX_GROUP_NAME_LENGTH)
            setValue(value)
            setResponder(responder)
        },
    )

    private fun addPagedRows(rows: List<Pair<String, String>>, left: Int, top: Int, panelWidth: Int, rowsPerPage: Int, action: (String) -> Unit) {
        val pageCount = ((rows.size + rowsPerPage - 1) / rowsPerPage).coerceAtLeast(1)
        page = page.coerceIn(0, pageCount - 1)
        rows.drop(page * rowsPerPage).take(rowsPerPage).forEachIndexed { index, (label, value) ->
            addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.literal(label)) { action(value) }.bounds(left, top + index * 22, panelWidth, 20).build())
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
            if (expandable) add("  ".repeat(depth) + marker + node.segment to NODE_PREFIX + path)
            node.group?.let { add("  ".repeat(depth + if (expandable) 1 else 0) + "  " + it.fullName to it.fullName) }
            if (expandable && path in expanded) addAll(flatten(node.children, expanded, depth + 1))
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

    private fun colorSelected() {
        val selected = selectedName ?: return
        val parsed = if (color == "-1") -1 else color.removePrefix("#").takeIf { it.length == 6 }?.toIntOrNull(16)
        if (parsed == null || parsed !in -1..0xffffff) {
            status = ModText.NETWORK_MANAGER_INVALID_COLOR.text
            return
        }
        send(NetworkManagerGroupMessage.Operation.COLOR, selected, color = parsed)
        status = ModText.NETWORK_MANAGER_REQUEST_SENT.text
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
        val level = minecraft?.level ?: return
        val parsedRange = range.toIntOrNull()
        if (parsedRange == null || parsedRange !in NetworkManagerClientSettings.MIN_RANGE..NetworkManagerClientSettings.MAX_RANGE) {
            status = ModText.NETWORK_MANAGER_INVALID_RANGE.format(NetworkManagerClientSettings.MIN_RANGE, NetworkManagerClientSettings.MAX_RANGE)
            return
        }
        val old = NetworkManagerClientSettings.get(level.dimension().location(), pos)
        NetworkManagerClientSettings.set(level.dimension().location(), pos, old.copy(delimiter = delimiter, range = parsedRange))
        status = ModText.NETWORK_MANAGER_SETTINGS_SAVED.text
        rebuild()
    }

    private fun toggleExpansion(path: String) {
        val level = minecraft?.level ?: return
        val settings = NetworkManagerClientSettings.get(level.dimension().location(), pos)
        val expanded = settings.expandedPaths.toMutableSet()
        if (!expanded.add(path)) expanded.remove(path)
        NetworkManagerClientSettings.set(level.dimension().location(), pos, settings.copy(expandedPaths = expanded))
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
        if (tab != Tab.GROUPS || !::searchBox.isInitialized) return
        focusedField = when {
            renameBox.isFocused -> "rename"
            colorBox.isFocused -> "color"
            delimiterBox.isFocused -> "delimiter"
            rangeBox.isFocused -> "range"
            else -> "search"
        }
    }

    override fun resize(minecraft: net.minecraft.client.Minecraft, width: Int, height: Int) {
        rememberFocus()
        super.resize(minecraft, width, height)
    }

    private fun unavailable() {
        minecraft?.player?.displayClientMessage(ModText.NETWORK_MANAGER_UNAVAILABLE.text, true)
        onClose()
    }

    private fun stateSnapshot(manager: NetworkManagerBlockEntity): String = manager.peripheralGroups.toSortedMap().entries.joinToString("|") { (name, group) -> "$name:${group.color}:${group.peripherals.sorted()}" } + manager.displayPeripherals.keys.sorted() + minecraft?.player?.mainHandItem?.let(UltimateConfigurator::getSelectedNetworkGroup)

    override fun tick() {
        val manager = manager ?: return unavailable()
        val current = stateSnapshot(manager)
        if (rebuildRequested || current != snapshot) {
            rebuildRequested = false
            rebuild()
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (tab == Tab.GROUPS && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            when {
                searchBox.isFocused -> create()
                renameBox.isFocused -> renameSelected()
                colorBox.isFocused -> colorSelected()
                delimiterBox.isFocused || rangeBox.isFocused -> saveSettings()
            }
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(graphics)
        super.render(graphics, mouseX, mouseY, partialTick)
        graphics.drawCenteredString(font, title, width / 2, 8, 0xffffff)
        selectedName?.let { graphics.drawCenteredString(font, ModText.NETWORK_MANAGER_SELECTED.format(it), width / 2, height - 22, 0xa0ffa0) }
        if (status != CommonComponents.EMPTY) graphics.drawCenteredString(font, status, width / 2, height - 10, 0xffd060)
    }

    companion object {
        private const val NODE_PREFIX = "\u0000"
    }
}
