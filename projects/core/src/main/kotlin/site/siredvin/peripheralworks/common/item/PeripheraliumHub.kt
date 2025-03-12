package site.siredvin.peripheralworks.common.item

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import site.siredvin.broccolium.modules.base.item.HiddenDescriptiveItemItem
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.turtles.TurtlePeripheraliumHubPeripheral
import site.siredvin.peripheralworks.data.ModTooltip
import site.siredvin.tweakium.modules.platform.ComputerPlatformToolkit
import java.util.function.Function
import java.util.function.Supplier

class PeripheraliumHub(
    properties: Properties,
    enableSup: Supplier<Boolean>,
    alwaysShow: Boolean,
    vararg tooltipHook: Function<HiddenDescriptiveItemItem, List<Component>>,
) : HiddenDescriptiveItemItem(properties, enableSup, alwaysShow, *tooltipHook) {

    override fun appendHoverText(
        itemStack: ItemStack,
        context: TooltipContext,
        list: MutableList<Component>,
        tooltipFlag: TooltipFlag,
    ) {
        super.appendHoverText(itemStack, context, list, tooltipFlag)
        if (InputConstants.isKeyDown(Minecraft.getInstance().window.window, InputConstants.KEY_LSHIFT)) {
            val storedData = itemStack.get(DataComponents.CUSTOM_DATA)?.copyTag() ?: return
            if (!storedData.isEmpty) {
                val connectedUpgrades = storedData.getList(PeripheraliumHubPeripheral.UPGRADES_TAG, 8)
                val activeMode = storedData.getString(PeripheraliumHubPeripheral.MODE_TAG)
                if (connectedUpgrades.isNotEmpty() && activeMode.isNotEmpty()) {
                    val isTurtle = activeMode == TurtlePeripheraliumHubPeripheral.TURTLE_MODE
                    if (isTurtle) {
                        list.add(ModTooltip.PERIPHERALIUM_HUB_TURTLE.text)
                        list.add(ModTooltip.PERIPHERALIUM_HUB_STORED.text)
                        connectedUpgrades.forEach {
                            val turtleUpgrade = ComputerPlatformToolkit.get().getTurtleUpgrade(it.asString)
                            if (turtleUpgrade != null) {
                                list.add(Component.literal("    ").append(turtleUpgrade.craftingItem.hoverName))
                            }
                        }
                    } else {
                        list.add(ModTooltip.PERIPHERALIUM_HUB_POCKET.text)
                        list.add(ModTooltip.PERIPHERALIUM_HUB_STORED.text)
                        connectedUpgrades.forEach {
                            val pocketUpgrade = ComputerPlatformToolkit.get().getPocketUpgrade(it.asString)
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
