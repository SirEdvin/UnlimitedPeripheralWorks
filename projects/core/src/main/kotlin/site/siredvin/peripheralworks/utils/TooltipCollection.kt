package site.siredvin.peripheralworks.utils

import net.minecraft.network.chat.Component
import site.siredvin.peripheralium.common.items.PeripheralBlockItem
import site.siredvin.peripheralium.common.items.PeripheralItem
import site.siredvin.peripheralium.util.text
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.operations.SphereOperations
import java.util.function.Function
import java.util.function.Supplier

object TooltipCollection {
    fun isDisabled(item: PeripheralItem): List<Component> {
        if (item.isEnabled()) {
            return emptyList()
        }
        return listOf(text(PeripheralWorksCore.MOD_ID, "item_disabled"))
    }

    fun isDisabled(item: PeripheralBlockItem): List<Component> {
        if (item.isEnabled()) {
            return emptyList()
        }
        return listOf(text(PeripheralWorksCore.MOD_ID, "item_disabled"))
    }

    fun buildMaxPeripheralsCount(maxCountSup: Supplier<Int>): Function<PeripheralItem, List<Component>> {
        return Function { listOf(text(PeripheralWorksCore.MOD_ID, "peripheralium_hub_max_peripherals", maxCountSup.get())) }
    }

    fun universalScanningRadius(item: PeripheralBlockItem): List<Component> {
        return listOf(
            text(PeripheralWorksCore.MOD_ID, "universal_scanner_free_range", SphereOperations.PORTABLE_UNIVERSAL_SCAN.maxFreeRadius),
            text(PeripheralWorksCore.MOD_ID, "universal_scanner_max_range", SphereOperations.PORTABLE_UNIVERSAL_SCAN.maxCostRadius),
        )
    }

    fun remoteObserverRadius(item: PeripheralBlockItem): List<Component> {
        return listOf(
            text(PeripheralWorksCore.MOD_ID, "remote_observer_range", PeripheralWorksConfig.remoteObserverMaxRange),
        )
    }
}
