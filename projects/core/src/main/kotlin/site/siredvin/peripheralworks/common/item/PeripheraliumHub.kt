package site.siredvin.peripheralworks.common.item

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.common.items.PeripheralItem
import site.siredvin.peripheralium.computercraft.pocket.StatefulPocketUpgrade
import site.siredvin.peripheralium.xplat.PeripheraliumPlatform
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.turtles.TurtlePeripheraliumHubPeripheral
import site.siredvin.peripheralworks.data.ModTooltip
import java.util.function.Function
import java.util.function.Supplier

class PeripheraliumHub(
    properties: Properties,
    enableSup: Supplier<Boolean>,
    alwaysShow: Boolean,
    vararg tooltipHook: Function<PeripheralItem, List<Component>>,
) : PeripheralItem(properties, enableSup, alwaysShow, *tooltipHook) {

    override fun appendHoverText(
        itemStack: ItemStack,
        level: Level?,
        list: MutableList<Component>,
        tooltipFlag: TooltipFlag,
    ) {
        super.appendHoverText(itemStack, level, list, tooltipFlag)
        if (InputConstants.isKeyDown(Minecraft.getInstance().window.window, InputConstants.KEY_LSHIFT)) {
            val storedData = itemStack.getTagElement(StatefulPocketUpgrade.STORED_DATA_TAG)
            if (storedData != null && !storedData.isEmpty) {
                val connectedUpgrades = storedData.getList(PeripheraliumHubPeripheral.UPGRADES_TAG, 8)
                val activeMode = storedData.getString(PeripheraliumHubPeripheral.MODE_TAG)
                if (connectedUpgrades.isNotEmpty() && activeMode.isNotEmpty()) {
                    val isTurtle = activeMode == TurtlePeripheraliumHubPeripheral.TURTLE_MODE
                    if (isTurtle) {
                        list.add(ModTooltip.PERIPHERALIUM_HUB_TURTLE.text)
                        list.add(ModTooltip.PERIPHERALIUM_HUB_STORED.text)
                        connectedUpgrades.forEach {
                            val turtleUpgrade = PeripheraliumPlatform.getTurtleUpgrade(it.asString)
                            if (turtleUpgrade != null) {
                                list.add(Component.literal("    ").append(turtleUpgrade.craftingItem.hoverName))
                            }
                        }
                    } else {
                        list.add(ModTooltip.PERIPHERALIUM_HUB_POCKET.text)
                        list.add(ModTooltip.PERIPHERALIUM_HUB_STORED.text)
                        connectedUpgrades.forEach {
                            val pocketUpgrade = PeripheraliumPlatform.getPocketUpgrade(it.asString)
                            if (pocketUpgrade != null) {
                                list.add(Component.literal("    ").append(pocketUpgrade.craftingItem.hoverName))
                            }
                        }
                    }
                }
            }
        }
    }
}
