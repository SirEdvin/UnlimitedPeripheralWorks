package site.siredvin.peripheralworks.utils

import net.minecraft.network.chat.Component
import site.siredvin.peripheralium.common.items.PeripheralBlockItem
import site.siredvin.peripheralium.common.items.PeripheralItem
import site.siredvin.peripheralium.util.text
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.computercraft.operations.SphereOperations
import java.util.function.Function
import java.util.function.Supplier

object TooltipCollection {
    fun isDisabled(item: PeripheralItem): Component? {
        if (!item.isEnabled())
            return text(PeripheralWorksCore.MOD_ID, "item_disabled")
        return null
    }

    fun isDisabled(item: PeripheralBlockItem): Component? {
        if (!item.isEnabled())
            return text(PeripheralWorksCore.MOD_ID, "item_disabled")
        return null
    }

    fun buildMaxPeripheralsCount(maxCountSup: Supplier<Int>): Function<PeripheralItem, Component?> {
        return Function { text(PeripheralWorksCore.MOD_ID, "peripheralium_hub_max_peripherals", maxCountSup.get()) }
    }

    fun universalScanningFreeRadius(item: PeripheralBlockItem): Component? {
        if (!item.isEnabled())
            return null
        return text(PeripheralWorksCore.MOD_ID, "universal_scanner_free_range", SphereOperations.UNIVERSAL_SCAN.maxFreeRadius)
    }

    fun universalScanningMaxRadius(item: PeripheralBlockItem): Component? {
        if (!item.isEnabled())
            return null
        return text(PeripheralWorksCore.MOD_ID, "universal_scanner_max_range", SphereOperations.UNIVERSAL_SCAN.maxCostRadius)
    }
}