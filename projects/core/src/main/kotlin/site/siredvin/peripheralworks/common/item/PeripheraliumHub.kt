package site.siredvin.peripheralworks.common.item

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import site.siredvin.broccolium.modules.base.item.HiddenDescriptiveItemItem
import site.siredvin.peripheralworks.common.setup.ModDataComponents
import site.siredvin.peripheralworks.data.ModTooltip
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
            val turtleData = itemStack.get(ModDataComponents.TURTLE_UPGRADES.get())
            val pocketData = itemStack.get(ModDataComponents.POCKET_UPGRADES.get())
            if (turtleData != null) {
                list.add(ModTooltip.PERIPHERALIUM_HUB_TURTLE.text)
                list.add(ModTooltip.PERIPHERALIUM_HUB_STORED.text)
                turtleData.upgrades.forEach {
                    list.add(Component.literal("    ").append(it.upgradeItem.hoverName))
                }
            } else if (pocketData != null) {
                list.add(ModTooltip.PERIPHERALIUM_HUB_POCKET.text)
                list.add(ModTooltip.PERIPHERALIUM_HUB_STORED.text)
                pocketData.upgrades.forEach {
                    list.add(Component.literal("    ").append(it.upgradeItem.hoverName))
                }
            }
        }
    }
}
