package site.siredvin.peripheralworks.testmod

import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestAssertException
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.nbt.NbtUtils
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralworks.client.configurator.NetworkManagerClientSettings
import site.siredvin.peripheralworks.client.configurator.NetworkManagerScreen
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
import site.siredvin.peripheralworks.common.item.UltimateConfigurator
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.peripheralworks.subsystem.configurator.NetworkManagerMode
import site.siredvin.testiarium.api.ClientGameTest
import site.siredvin.testiarium.api.TestGroup
import site.siredvin.testiarium.fixture.client.thenOnClient

@TestGroup("network-manager-client")
class NetworkManagerClientGameTests {
    @ClientGameTest(template = "empty", timeoutTicks = 800)
    fun createsHierarchyAndAddsPeripherals(helper: GameTestHelper) {
        val managerPos = BlockPos(1, 1, 1)
        helper.startSequence()
            .thenExecute {
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
                NetworkManagerClientSettings.set(minecraft.level!!.dimension().location(), helper.absolutePos(managerPos), NetworkManagerClientSettings.Settings())
                val player = minecraft.player ?: error("Client player is missing")
                player.xRot = -90f
                minecraft.gameMode!!.useItem(player, InteractionHand.MAIN_HAND)
                check(minecraft.screen is NetworkManagerScreen) { "Bound configurator did not open the network manager screen" }
                createGroup(minecraft.screen as NetworkManagerScreen, "factory/ore/iron")
            }
            .thenWaitUntil { manager(helper, managerPos).requireGroup("factory/ore/iron") }
            .thenOnClient { createGroup(minecraft.screen as NetworkManagerScreen, "factory/ore/gold") }
            .thenWaitUntil { manager(helper, managerPos).requireGroup("factory/ore/gold") }
            .thenIdle(5)
            .thenOnClient {
                val screen = minecraft.screen as NetworkManagerScreen
                editBoxes(screen).first().setValue("")
                screen.tick()
                click(screen, button(screen, "+ factory", trim = true))
                click(screen, button(screen, ">"))
                click(screen, button(screen, "+ ore", trim = true))
                editBoxes(screen).first().setValue("factory/ore/iron")
                screen.tick()
                click(screen, button(screen, "factory/ore/iron", trim = true))
            }
            .thenWaitUntil {
                if (UltimateConfigurator.getSelectedNetworkGroup(player(helper).mainHandItem) != "factory/ore/iron") retry("Selected group has not synchronized")
            }
            .thenIdle(5)
            .thenOnClient {
                val screen = minecraft.screen as NetworkManagerScreen
                click(screen, button(screen, ModText.NETWORK_MANAGER_TAB_MEMBERSHIP.text.string))
                click(screen, button(screen, "[ ] monitor_0"))
                click(screen, button(screen, "[ ] printer_0"))
            }
            .thenWaitUntil {
                val members = manager(helper, managerPos).peripheralGroups.getValue("factory/ore/iron").peripherals
                if (members != setOf("monitor_0", "printer_0")) retry("Peripheral memberships have not synchronized")
            }
            .thenSucceed()
    }

    private fun createGroup(screen: NetworkManagerScreen, name: String) {
        editBoxes(screen).first().setValue(name)
        click(screen, button(screen, ModText.NETWORK_MANAGER_CREATE.text.string))
    }

    private fun manager(helper: GameTestHelper, pos: BlockPos) = helper.getBlockEntity(pos) as NetworkManagerBlockEntity

    private fun player(helper: GameTestHelper) = helper.level.randomPlayer ?: error("Client GameTest player is missing")

    private fun editBoxes(screen: NetworkManagerScreen) = screen.children().filterIsInstance<EditBox>().sortedWith(compareBy({ it.y }, { it.x }))

    private fun button(screen: NetworkManagerScreen, label: String, trim: Boolean = false): Button = screen.children().filterIsInstance<Button>().singleOrNull {
        (if (trim) it.message.string.trim() else it.message.string) == label
    } ?: error("Button '$label' not found among ${screen.children().filterIsInstance<Button>().map { it.message.string }}")

    private fun click(screen: NetworkManagerScreen, widget: AbstractWidget) {
        check(screen.mouseClicked(widget.x + widget.width / 2.0, widget.y + widget.height / 2.0, 0)) { "Widget click was not handled: ${widget.message.string}" }
    }

    private fun NetworkManagerBlockEntity.requireGroup(name: String) {
        if (name !in peripheralGroups) retry("Group '$name' has not synchronized")
    }

    private fun retry(message: String): Nothing = throw GameTestAssertException(message)
}
