package site.siredvin.peripheralworks.testmod

import net.minecraft.client.gui.components.AbstractSliderButton
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestAssertException
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.nbt.NbtUtils
import net.minecraft.network.chat.CommonComponents
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralworks.client.configurator.NetworkManagerColorPickerScreen
import site.siredvin.peripheralworks.client.configurator.NetworkManagerScreen
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
import site.siredvin.peripheralworks.common.item.UltimateConfigurator
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.peripheralworks.subsystem.configurator.NetworkManagerMode
import site.siredvin.testiarium.api.ClientGameTest
import site.siredvin.testiarium.api.TestGroup
import site.siredvin.testiarium.fixture.client.ClientTestHelper
import site.siredvin.testiarium.fixture.client.thenOnClient
import java.io.File

@TestGroup("network-manager-client")
class NetworkManagerClientGameTests {
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
                stack.orCreateTag.putString(UltimateConfigurator.ACTIVE_MOD_NAME, NetworkManagerMode.modeID.toString())
                stack.orCreateTag.put(UltimateConfigurator.ACTIVE_MOD_POS, NbtUtils.writeBlockPos(helper.absolutePos(managerPos)))
                stack.orCreateTag.putString(UltimateConfigurator.ACTIVE_MOD_DIMENSION, helper.level.dimension().location().toString())
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
                click(screen, button(screen, ModText.NETWORK_MANAGER_VISUALIZATION.format(ModText.NETWORK_MANAGER_VISUALIZATION_ALL.text).string))
            }
            .thenWaitUntil {
                if (manager(helper, managerPos).range != 64 || NetworkManagerMode.getVisualizationMode(player(helper).mainHandItem) != NetworkManagerMode.VisualizationMode.SELECTED) {
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
            .thenOnClient {
                click(minecraft.screen as NetworkManagerScreen, button(minecraft.screen as NetworkManagerScreen, ModText.NETWORK_MANAGER_VISIBILITY.format(ModText.NETWORK_MANAGER_VISIBILITY_DEFAULT.text).string))
            }
            .thenWaitUntil {
                if (manager(helper, managerPos).peripheralGroups.getValue("factory/ore/iron").visibility != NetworkManagerBlockEntity.GroupVisibility.SHOW) retry("Group visibility has not synchronized")
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

    private fun screenshot(name: String) = ClientTestHelper().screenshot(name)

    private fun requireScreenshot(name: String) {
        if (!screenshotFile(name).isFile) retry("Screenshot '$name' is not saved")
    }

    private fun screenshotFile(name: String) = File(System.getProperty("testiarium.screenshots"), "screenshots/$name")

    private fun manager(helper: GameTestHelper, pos: BlockPos) = helper.getBlockEntity(pos) as NetworkManagerBlockEntity

    private fun player(helper: GameTestHelper) = helper.level.randomPlayer ?: error("Client GameTest player is missing")

    private fun editBoxes(screen: Screen) = screen.children().filterIsInstance<EditBox>().sortedWith(compareBy({ it.y }, { it.x }))

    private fun findButton(screen: Screen, label: String, trim: Boolean = false): Button? = screen.children().filterIsInstance<Button>().singleOrNull {
        (if (trim) it.message.string.trim() else it.message.string) == label
    }

    private fun button(screen: Screen, label: String, trim: Boolean = false): Button = findButton(screen, label, trim)
        ?: error("Button '$label' not found among ${screen.children().filterIsInstance<Button>().map { it.message.string }}")

    private fun click(screen: Screen, widget: AbstractWidget) {
        check(screen.mouseClicked(widget.x + widget.width / 2.0, widget.y + widget.height / 2.0, 0)) { "Widget click was not handled: ${widget.message.string}" }
    }

    private fun NetworkManagerBlockEntity.requireGroup(name: String) {
        if (name !in peripheralGroups) retry("Group '$name' has not synchronized")
    }

    private fun retry(message: String): Nothing = throw GameTestAssertException(message)
}
