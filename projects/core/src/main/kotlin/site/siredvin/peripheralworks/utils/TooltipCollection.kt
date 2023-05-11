package site.siredvin.peripheralworks.utils

import net.minecraft.network.chat.Component
import site.siredvin.peripheralium.common.items.PeripheralItem
import site.siredvin.peripheralium.util.text
import site.siredvin.peripheralworks.PeripheralWorksCore
import java.util.function.Function
import java.util.function.Supplier

object TooltipCollection {
    fun isDisabled(item: PeripheralItem): Component? {
        if (!item.isEnabled())
            return text(PeripheralWorksCore.MOD_ID, "item_disabled")
        return null
    }

    fun buildMaxPeripheralsCount(maxCountSup: Supplier<Int>): Function<PeripheralItem, Component?> {
        return Function { text(PeripheralWorksCore.MOD_ID, "peripheralium_hub_max_peripherals", maxCountSup.get()) }
    }
}