package site.siredvin.peripheralworks.testmod

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.AbstractSliderButton
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.gametest.framework.GameTestAssertException
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.nbt.NbtUtils
import net.minecraft.network.chat.CommonComponents
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Pose
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import site.siredvin.peripheralworks.client.configurator.ConfiguratorFavoriteEditScreen
import site.siredvin.peripheralworks.client.configurator.ConfiguratorTargetHistoryScreen
import site.siredvin.peripheralworks.client.configurator.NetworkManagerColorPickerScreen
import site.siredvin.peripheralworks.client.configurator.NetworkManagerScreen
import site.siredvin.peripheralworks.client.configurator.TargetRenderSettingsScreen
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
import site.siredvin.peripheralworks.common.blockentity.PeripheralProxyBlockEntity
import site.siredvin.peripheralworks.common.blockentity.RemoteObserverBlockEntity
import site.siredvin.peripheralworks.common.item.UltimateConfigurator
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.peripheralworks.subsystem.configurator.BoxStyle
import site.siredvin.peripheralworks.subsystem.configurator.NetworkManagerMode
import site.siredvin.peripheralworks.subsystem.configurator.PeripheralProxyMode
import site.siredvin.peripheralworks.subsystem.configurator.RemoteObserverMode
import site.siredvin.peripheralworks.subsystem.configurator.TextStyle
import site.siredvin.testiarium.api.ClientGameTest
import site.siredvin.testiarium.api.TestGroup
import site.siredvin.testiarium.fixture.client.ClientTestHelper
import site.siredvin.testiarium.fixture.client.thenOnClient
import java.io.File

@TestGroup("network-manager-client")
class NetworkManagerClientGameTests {
    @ClientGameTest(template = "empty", timeoutTicks = 600)
    fun managesDetachedConfiguratorTargetsAndPreservesAttachedUse(helper: GameTestHelper) {
        val proxyPos = BlockPos(1, 1, 1)
        val observerPos = BlockPos(3, 1, 1)
        helper.startSequence()
            .thenExecute {
                helper.setBlock(proxyPos, Blocks.PERIPHERAL_PROXY.get())
                helper.setBlock(observerPos, Blocks.REMOTE_OBSERVER.get())
                val configurator = Items.ULTIMATE_CONFIGURATOR.get() as UltimateConfigurator
                val stack = ItemStack(configurator)
                configurator.saveActiveMode(stack, PeripheralProxyMode, helper.absolutePos(proxyPos), helper.level)
                val proxyTarget = configurator.getRecentTargets(stack).first()
                check(configurator.toggleFavorite(stack, proxyTarget) == UltimateConfigurator.FavoriteResult.ADDED)
                check(configurator.renameFavorite(stack, proxyTarget, "Workshop Proxy"))
                repeat(3) { index ->
                    configurator.saveActiveMode(stack, RemoteObserverMode, helper.absolutePos(BlockPos(5 + index, 1, 1)), helper.level)
                    check(configurator.toggleFavorite(stack, configurator.getRecentTargets(stack).first()) == UltimateConfigurator.FavoriteResult.ADDED)
                }
                configurator.saveActiveMode(stack, RemoteObserverMode, helper.absolutePos(observerPos), helper.level)
                configurator.clearActiveMode(stack)
                player(helper).setItemInHand(InteractionHand.MAIN_HAND, stack)
            }
            .thenIdle(5)
            .thenOnClient {
                val player = minecraft.player ?: error("Client player is missing")
                player.xRot = -90f
                minecraft.gameMode!!.useItem(player, InteractionHand.MAIN_HAND)
                val screen = minecraft.screen as? ConfiguratorTargetHistoryScreen ?: error("Detached configurator target screen did not open")
                val labels = screen.children().filterIsInstance<Button>().map { it.message.string }
                val observerLabel = targetLabel(Blocks.REMOTE_OBSERVER.get().name.string, helper.absolutePos(observerPos))
                check(observerLabel in labels) { "Recent target did not render its block name, dimension, and coordinates: $labels" }
                check("Workshop Proxy" in labels && labels.none { it.startsWith("Workshop Proxy |") }) { "Custom favorite name did not hide its default label" }
                check(screen.children().filterIsInstance<Button>().count { it.message == ModText.CONFIGURATOR_HISTORY_EDIT.text } == 4) { "Favorite page did not display four rows" }
                check(editBoxes(screen).isEmpty()) { "Favorite rows still contain inline name fields" }
                check(ModText.CONFIGURATOR_HISTORY_UNFAVORITE.text.string !in labels) { "Recent rows still allow removing favorites" }
                click(screen, button(screen, ModText.CONFIGURATOR_HISTORY_FAVORITE.text.string))
            }
            .thenWaitUntil {
                val configurator = player(helper).mainHandItem.item as UltimateConfigurator
                if (configurator.getFavoriteTargets(player(helper).mainHandItem).size != 5) retry("Favorite toggle has not synchronized")
            }
            .thenIdle(3)
            .thenOnClient {
                val screen = minecraft.screen as? ConfiguratorTargetHistoryScreen ?: error("Target screen closed after favorite toggle")
                click(screen, button(screen, ModText.CONFIGURATOR_HISTORY_EDIT.text.string))
                val editor = minecraft.screen as? ConfiguratorFavoriteEditScreen ?: error("Favorite editor did not open")
                val name = editBoxes(editor).single()
                name.setValue("n".repeat(65))
                check(name.value.length == 64) { "Favorite name field did not enforce the 64-character limit" }
                name.setValue("Roof Observer")
                click(editor, button(editor, ModText.CONFIGURATOR_HISTORY_APPLY.text.string))
            }
            .thenWaitUntil {
                val configurator = player(helper).mainHandItem.item as UltimateConfigurator
                if (configurator.getFavoriteTargets(player(helper).mainHandItem).first().name != "Roof Observer") retry("Favorite rename has not synchronized")
            }
            .thenIdle(3)
            .thenOnClient {
                val screen = minecraft.screen as ConfiguratorTargetHistoryScreen
                click(screen, button(screen, ModText.CONFIGURATOR_HISTORY_EDIT.text.string))
                val editor = minecraft.screen as ConfiguratorFavoriteEditScreen
                editBoxes(editor).single().setValue("")
                click(editor, button(editor, ModText.CONFIGURATOR_HISTORY_APPLY.text.string))
            }
            .thenWaitUntil {
                val configurator = player(helper).mainHandItem.item as UltimateConfigurator
                if (configurator.getFavoriteTargets(player(helper).mainHandItem).first().name != null) retry("Favorite name reset has not synchronized")
            }
            .thenIdle(3)
            .thenOnClient {
                val screen = minecraft.screen as ConfiguratorTargetHistoryScreen
                click(screen, button(screen, ModText.CONFIGURATOR_HISTORY_EDIT.text.string))
                val editor = minecraft.screen as ConfiguratorFavoriteEditScreen
                click(editor, button(editor, ModText.CONFIGURATOR_HISTORY_UNFAVORITE.text.string))
            }
            .thenWaitUntil {
                val configurator = player(helper).mainHandItem.item as UltimateConfigurator
                if (configurator.getFavoriteTargets(player(helper).mainHandItem).size != 4) retry("Removing the favorite has not synchronized")
            }
            .thenIdle(3)
            .thenOnClient {
                val screen = minecraft.screen as ConfiguratorTargetHistoryScreen
                click(screen, button(screen, targetLabel(Blocks.REMOTE_OBSERVER.get().name.string, helper.absolutePos(observerPos))))
            }
            .thenWaitUntil {
                val configurator = player(helper).mainHandItem.item as UltimateConfigurator
                if (configurator.getActiveMode(player(helper).mainHandItem)?.second != helper.absolutePos(observerPos)) retry("Target selection has not attached the configurator")
            }
            .thenIdle(5)
            .thenOnClient {
                check(minecraft.screen !is ConfiguratorTargetHistoryScreen) { "Target screen did not close after successful selection" }
                val player = minecraft.player ?: error("Client player is missing")
                player.xRot = -90f
                minecraft.gameMode!!.useItem(player, InteractionHand.MAIN_HAND)
                check(minecraft.screen is TargetRenderSettingsScreen) { "Attached mode air use did not retain precedence" }
                minecraft.setScreen(null)
            }
            .thenSucceed()
    }

    @ClientGameTest(template = "empty", timeoutTicks = 400)
    fun configuresProxyAndObserverTargetRendering(helper: GameTestHelper) {
        val proxyPos = BlockPos(1, 1, 1)
        val observerPos = BlockPos(3, 1, 1)
        helper.startSequence()
            .thenExecute {
                helper.setBlock(proxyPos, Blocks.PERIPHERAL_PROXY.get())
                helper.setBlock(observerPos, Blocks.REMOTE_OBSERVER.get())
                bind(helper, PeripheralProxyMode.modeID, proxyPos)
            }
            .thenIdle(5)
            .thenOnClient {
                val player = minecraft.player ?: error("Client player is missing")
                player.xRot = -90f
                minecraft.gameMode!!.useItem(player, InteractionHand.MAIN_HAND)
                val screen = minecraft.screen as? TargetRenderSettingsScreen ?: error("Proxy settings screen did not open")
                click(screen, button(screen, textStyleLabel(TextStyle.REGULAR)))
                click(screen, button(screen, boxStyleLabel(BoxStyle.FLARE)), 1)
            }
            .thenWaitUntil {
                val proxy = helper.getBlockEntity(proxyPos) as PeripheralProxyBlockEntity
                if (proxy.textStyle != TextStyle.BOLD || proxy.boxStyle != BoxStyle.FILLED) retry("Proxy styles have not synchronized")
            }
            .thenOnClient { minecraft.setScreen(null) }
            .thenExecute { bind(helper, RemoteObserverMode.modeID, observerPos) }
            .thenIdle(5)
            .thenOnClient {
                val player = minecraft.player ?: error("Client player is missing")
                player.xRot = -90f
                minecraft.gameMode!!.useItem(player, InteractionHand.MAIN_HAND)
                val screen = minecraft.screen as? TargetRenderSettingsScreen ?: error("Observer settings screen did not open")
                click(screen, button(screen, textStyleLabel(TextStyle.NONE)), 1)
                click(screen, button(screen, boxStyleLabel(BoxStyle.FLARE)))
            }
            .thenWaitUntil {
                val observer = helper.getBlockEntity(observerPos) as RemoteObserverBlockEntity
                if (observer.textStyle != TextStyle.BOLD || observer.boxStyle != BoxStyle.NONE) retry("Observer styles have not synchronized")
            }
            .thenExecute { (helper.getBlockEntity(observerPos) as RemoteObserverBlockEntity).addPosToTrack(helper.absolutePos(BlockPos(4, 1, 1))) }
            .thenIdle(3)
            .thenOnClient {
                val observer = minecraft.level!!.getBlockEntity(helper.absolutePos(observerPos)) as RemoteObserverBlockEntity
                check(observer.trackedBlocksView.size == 1) { "Observer addition did not synchronize" }
            }
            .thenExecute { (helper.getBlockEntity(observerPos) as RemoteObserverBlockEntity).removePosToTrack(helper.absolutePos(BlockPos(4, 1, 1))) }
            .thenIdle(3)
            .thenOnClient {
                val observer = minecraft.level!!.getBlockEntity(helper.absolutePos(observerPos)) as RemoteObserverBlockEntity
                check(observer.trackedBlocksView.isEmpty()) { "Observer removal did not synchronize" }
            }
            .thenExecute { helper.setBlock(observerPos, net.minecraft.world.level.block.Blocks.AIR) }
            .thenIdle(5)
            .thenOnClient { check(minecraft.screen !is TargetRenderSettingsScreen) { "Unavailable observer screen remained open" } }
            .thenExecute {
                bind(helper, PeripheralProxyMode.modeID, proxyPos)
                val player = player(helper)
                player.pose = Pose.CROUCHING
                player.xRot = -90f
                (player.mainHandItem.item as UltimateConfigurator).use(helper.level, player, InteractionHand.MAIN_HAND)
            }
            .thenWaitUntil {
                if ((player(helper).mainHandItem.item as UltimateConfigurator).getActiveMode(player(helper).mainHandItem) != null) retry("Crouching air use did not detach the configurator")
            }
            .thenIdle(3)
            .thenOnClient { check(minecraft.screen !is TargetRenderSettingsScreen) { "Crouching air use opened settings" } }
            .thenSucceed()
    }

    @ClientGameTest(template = "empty", timeoutTicks = 800)
    fun createsHierarchyAndAddsPeripherals(helper: GameTestHelper) {
        val managerPos = BlockPos(1, 1, 1)
        helper.startSequence()
            .thenExecute {
                listOf("network-manager-group-created-iron.png", "network-manager-group-hierarchy.png", "network-manager-group-memberships.png").forEach {
                    screenshotFile(it).apply {
                        parentFile.mkdirs()
                        delete()
                    }
                }
                helper.setBlock(managerPos, Blocks.NETWORK_MANAGER.get())
                val manager = manager(helper, managerPos)
                manager.peripherals["monitor_0"] = helper.absolutePos(BlockPos(2, 1, 1))
                manager.peripherals["printer_0"] = helper.absolutePos(BlockPos(3, 1, 1))
                manager.pushData()

                val stack = ItemStack(Items.ULTIMATE_CONFIGURATOR.get())
                CustomData.update(DataComponents.CUSTOM_DATA, stack) {
                    it.putString(UltimateConfigurator.ACTIVE_MOD_NAME, NetworkManagerMode.modeID.toString())
                    it.put(UltimateConfigurator.ACTIVE_MOD_POS, NbtUtils.writeBlockPos(helper.absolutePos(managerPos)))
                    it.putString(UltimateConfigurator.ACTIVE_MOD_DIMENSION, helper.level.dimension().location().toString())
                }
                NetworkManagerMode.setVisualizationMode(stack, NetworkManagerMode.VisualizationMode.SELECTED_AND_UNGROUPED)
                player(helper).setItemInHand(InteractionHand.MAIN_HAND, stack)
            }
            .thenIdle(5)
            .thenOnClient {
                val player = minecraft.player ?: error("Client player is missing")
                player.xRot = -90f
                minecraft.gameMode!!.useItem(player, InteractionHand.MAIN_HAND)
                check(minecraft.screen is NetworkManagerScreen) { "Bound configurator did not open the network manager screen" }
                val screen = minecraft.screen as NetworkManagerScreen
                check(findButton(screen, ModText.NETWORK_MANAGER_SAVE_SETTINGS.text.string) == null) { "Settings leaked into the Groups tab" }
                click(screen, button(screen, ModText.NETWORK_MANAGER_TAB_SETTINGS.text.string))
                check(editBoxes(screen).size == 2) { "Settings tab did not expose delimiter and range" }
                editBoxes(screen)[1].setValue("64")
                click(screen, button(screen, ModText.NETWORK_MANAGER_SAVE_SETTINGS.text.string))
                click(screen, button(screen, textStyleLabel(TextStyle.REGULAR)))
                click(screen, button(screen, textStyleLabel(TextStyle.NONE)), 1)
                click(screen, button(screen, textStyleLabel(TextStyle.REGULAR)), 1)
                click(screen, button(screen, boxStyleLabel(BoxStyle.NONE)))
                click(screen, button(screen, boxStyleLabel(BoxStyle.NONE)), 1)
                click(screen, button(screen, boxStyleLabel(BoxStyle.NONE)))
                click(screen, button(screen, boxStyleLabel(BoxStyle.OUTLINE)))
            }
            .thenWaitUntil {
                val stack = player(helper).mainHandItem
                if (
                    manager(helper, managerPos).range != 64 ||
                    NetworkManagerMode.getTextStyle(stack, NetworkManagerMode.RenderTarget.SELECTED) != TextStyle.BOLD ||
                    NetworkManagerMode.getTextStyle(stack, NetworkManagerMode.RenderTarget.GROUPED) != TextStyle.BOLD ||
                    NetworkManagerMode.getTextStyle(stack, NetworkManagerMode.RenderTarget.UNGROUPED) != TextStyle.NONE ||
                    NetworkManagerMode.getBoxStyle(stack, NetworkManagerMode.RenderTarget.SELECTED) != BoxStyle.FILLED ||
                    NetworkManagerMode.getBoxStyle(stack, NetworkManagerMode.RenderTarget.GROUPED) != BoxStyle.FLARE ||
                    NetworkManagerMode.getBoxStyle(stack, NetworkManagerMode.RenderTarget.UNGROUPED) != BoxStyle.OUTLINE
                ) {
                    retry("Manager settings have not reached the server")
                }
            }
            .thenIdle(3)
            .thenOnClient {
                val screen = minecraft.screen as NetworkManagerScreen
                check(editBoxes(screen)[1].value == "64") { "Manager settings have not synchronized to the client" }
                click(screen, button(screen, ModText.NETWORK_MANAGER_TAB_GROUPS.text.string))
                createGroup(screen, "factory/ore/iron")
            }
            .thenWaitUntil { manager(helper, managerPos).requireGroup("factory/ore/iron") }
            .thenIdle(3)
            .thenOnClient { screenshot("network-manager-group-created-iron.png") }
            .thenWaitUntil { requireScreenshot("network-manager-group-created-iron.png") }
            .thenOnClient { createGroup(minecraft.screen as NetworkManagerScreen, "factory/ore/gold") }
            .thenWaitUntil { manager(helper, managerPos).requireGroup("factory/ore/gold") }
            .thenOnClient { createGroup(minecraft.screen as NetworkManagerScreen, "factory") }
            .thenWaitUntil { manager(helper, managerPos).requireGroup("factory") }
            .thenIdle(5)
            .thenOnClient {
                val screen = minecraft.screen as NetworkManagerScreen
                editBoxes(screen).first().setValue("")
                screen.tick()
                check(screen.children().filterIsInstance<Button>().count { it.message.string.trim() == "+ factory" } == 1) { "Collapsed group exposed a child row" }
                check(findButton(screen, "factory", trim = true) == null) { "Collapsed real group remained selectable" }
                click(screen, button(screen, "+ factory", trim = true))
                while (findButton(screen, "|- + ore", trim = true) == null) {
                    val next = button(screen, ">")
                    check(next.active) { "Expanded child group was not reachable through pagination" }
                    click(screen, next)
                }
                click(screen, button(screen, "|- + ore", trim = true))
            }
            .thenWaitUntil {
                val paths = NetworkManagerMode.getExpandedGroupPaths(player(helper).mainHandItem)
                if (!paths.containsAll(setOf("factory", "factory/ore"))) retry("Expanded hierarchy paths have not synchronized to the configurator")
            }
            .thenOnClient { screenshot("network-manager-group-hierarchy.png") }
            .thenWaitUntil { requireScreenshot("network-manager-group-hierarchy.png") }
            .thenOnClient {
                val screen = minecraft.screen as NetworkManagerScreen
                editBoxes(screen).first().setValue("factory/ore/iron")
                screen.tick()
                click(screen, button(screen, "factory/ore/iron", trim = true))
            }
            .thenWaitUntil {
                if (NetworkManagerMode.getSelectedGroup(player(helper).mainHandItem) != "factory/ore/iron") retry("Selected group has not synchronized")
            }
            .thenOnClient {
                val screen = minecraft.screen as NetworkManagerScreen
                val pipette = screen.children().filterIsInstance<Button>().single { it.message == CommonComponents.EMPTY && it.width == 20 }
                click(screen, pipette)
                val picker = minecraft.screen as? NetworkManagerColorPickerScreen ?: error("Pipette button did not open the RGB picker")
                check(picker.children().filterIsInstance<AbstractSliderButton>().size == 3) { "RGB picker did not expose three channels" }
                editBoxes(picker).single().setValue("#123456")
                click(picker, button(picker, ModText.NETWORK_MANAGER_APPLY.text.string))
            }
            .thenWaitUntil {
                if (manager(helper, managerPos).peripheralGroups.getValue("factory/ore/iron").color != 0x123456) retry("Picked group color has not synchronized")
            }
            .thenIdle(5)
            .thenOnClient {
                val screen = minecraft.screen as NetworkManagerScreen
                click(screen, button(screen, ModText.NETWORK_MANAGER_TAB_MEMBERSHIP.text.string))
                editBoxes(screen).single().setValue("monitor")
                screen.tick()
                check(findButton(screen, "[ ] printer_0") == null) { "Membership search did not filter by peripheral type" }
                editBoxes(screen).single().setValue("")
                screen.tick()
                click(screen, button(screen, "[ ] monitor_0"))
                click(screen, button(screen, "[ ] printer_0"))
            }
            .thenWaitUntil {
                val members = manager(helper, managerPos).peripheralGroups.getValue("factory/ore/iron").peripherals
                if (members != setOf("monitor_0", "printer_0")) retry("Peripheral memberships have not synchronized")
            }
            .thenOnClient { screenshot("network-manager-group-memberships.png") }
            .thenWaitUntil { requireScreenshot("network-manager-group-memberships.png") }
            .thenSucceed()
    }

    private fun createGroup(screen: NetworkManagerScreen, name: String) {
        editBoxes(screen).first().setValue(name)
        click(screen, button(screen, ModText.NETWORK_MANAGER_CREATE.text.string))
    }

    private fun textStyleLabel(style: TextStyle): String {
        val styleText = when (style) {
            TextStyle.NONE -> ModText.NETWORK_MANAGER_STYLE_NONE.text
            TextStyle.REGULAR -> ModText.NETWORK_MANAGER_TEXT_REGULAR.text
            TextStyle.BOLD -> ModText.NETWORK_MANAGER_TEXT_BOLD.text
        }
        return ModText.NETWORK_MANAGER_TEXT_STYLE.format(styleText).string
    }

    private fun boxStyleLabel(style: BoxStyle): String {
        val styleText = when (style) {
            BoxStyle.NONE -> ModText.NETWORK_MANAGER_STYLE_NONE.text
            BoxStyle.OUTLINE -> ModText.NETWORK_MANAGER_BOX_OUTLINE.text
            BoxStyle.FILLED -> ModText.NETWORK_MANAGER_BOX_FILLED.text
            BoxStyle.FLARE -> ModText.NETWORK_MANAGER_BOX_FLARE.text
        }
        return ModText.NETWORK_MANAGER_BOX_STYLE.format(styleText).string
    }

    private fun targetLabel(mode: String, pos: BlockPos): String = "$mode | ${playerDimension()} | ${pos.x}, ${pos.y}, ${pos.z}"

    private fun playerDimension() = Minecraft.getInstance().level?.dimension()?.location() ?: error("Client level is missing")

    private fun screenshot(name: String) = ClientTestHelper().screenshot(name)

    private fun requireScreenshot(name: String) {
        if (!screenshotFile(name).isFile) retry("Screenshot '$name' is not saved")
    }

    private fun screenshotFile(name: String) = File(System.getProperty("testiarium.screenshots"), "screenshots/$name")

    private fun manager(helper: GameTestHelper, pos: BlockPos) = helper.getBlockEntity(pos) as NetworkManagerBlockEntity

    private fun player(helper: GameTestHelper) = helper.level.randomPlayer ?: error("Client GameTest player is missing")

    private fun bind(helper: GameTestHelper, mode: net.minecraft.resources.ResourceLocation, pos: BlockPos) {
        val stack = ItemStack(Items.ULTIMATE_CONFIGURATOR.get())
        CustomData.update(DataComponents.CUSTOM_DATA, stack) {
            it.putString(UltimateConfigurator.ACTIVE_MOD_NAME, mode.toString())
            it.put(UltimateConfigurator.ACTIVE_MOD_POS, NbtUtils.writeBlockPos(helper.absolutePos(pos)))
            it.putString(UltimateConfigurator.ACTIVE_MOD_DIMENSION, helper.level.dimension().location().toString())
        }
        player(helper).setItemInHand(InteractionHand.MAIN_HAND, stack)
    }

    private fun editBoxes(screen: Screen) = screen.children().filterIsInstance<EditBox>().sortedWith(compareBy({ it.y }, { it.x }))

    private fun findButton(screen: Screen, label: String, trim: Boolean = false): Button? = screen.children().filterIsInstance<Button>().firstOrNull {
        (if (trim) it.message.string.trim() else it.message.string) == label
    }

    private fun button(screen: Screen, label: String, trim: Boolean = false): Button = findButton(screen, label, trim)
        ?: error("Button '$label' not found among ${screen.children().filterIsInstance<Button>().map { it.message.string }}")

    private fun click(screen: Screen, widget: AbstractWidget, button: Int = 0) {
        check(screen.mouseClicked(widget.x + widget.width / 2.0, widget.y + widget.height / 2.0, button)) { "Widget click was not handled: ${widget.message.string}" }
    }

    private fun NetworkManagerBlockEntity.requireGroup(name: String) {
        if (name !in peripheralGroups) retry("Group '$name' has not synchronized")
    }

    private fun retry(message: String): Nothing = throw GameTestAssertException(message)
}
