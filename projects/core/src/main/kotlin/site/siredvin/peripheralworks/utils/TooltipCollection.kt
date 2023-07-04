package site.siredvin.peripheralworks.utils

import net.minecraft.network.chat.Component
import site.siredvin.peripheralium.common.items.PeripheralBlockItem
import site.siredvin.peripheralium.common.items.PeripheralItem
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.operations.SphereOperations
import site.siredvin.peripheralworks.data.ModTooltip
import java.util.function.Function
import java.util.function.Supplier

object TooltipCollection {
    fun isDisabled(item: PeripheralItem): List<Component> {
        if (item.isEnabled()) {
            return emptyList()
        }
        return listOf(ModTooltip.ITEM_DISABLED.text)
    }

    fun isDisabled(item: PeripheralBlockItem): List<Component> {
        if (item.isEnabled()) {
            return emptyList()
        }
        return listOf(ModTooltip.ITEM_DISABLED.text)
    }

    fun buildMaxPeripheralsCount(maxCountSup: Supplier<Int>): Function<PeripheralItem, List<Component>> {
        return Function { listOf(ModTooltip.PERIPHERALIUM_HUB_MAX_PERIPHERALS.format(maxCountSup.get())) }
    }

    fun universalScanningRadius(item: PeripheralBlockItem): List<Component> {
        return listOf(
            ModTooltip.UNIVERSAL_SCANNER_FREE_RANGE.format(SphereOperations.PORTABLE_UNIVERSAL_SCAN.maxFreeRadius),
            ModTooltip.UNIVERSAL_SCANNER_MAX_RANGE.format(SphereOperations.PORTABLE_UNIVERSAL_SCAN.maxCostRadius),
        )
    }

    fun remoteObserverTooptips(item: PeripheralBlockItem): List<Component> {
        return listOf(
            ModTooltip.REMOTE_OBSERVER_RANGE.format(PeripheralWorksConfig.remoteObserverMaxRange),
            ModTooltip.REMOTE_OBSERVER_MAX_CAPACITY.format(PeripheralWorksConfig.remoteObserverMaxCapacity),
        )
    }

    fun peripheralProxyTooptips(item: PeripheralBlockItem): List<Component> {
        return listOf(
            ModTooltip.PERIPHERAL_PROXY_RANGE.format(PeripheralWorksConfig.peripheralProxyMaxRange),
            ModTooltip.PERIPHERAL_PROXY_MAX_CAPACITY.format(PeripheralWorksConfig.peripheralProxyMaxCapacity),
        )
    }

    fun realityForgerTooptips(item: PeripheralBlockItem): List<Component> {
        return listOf(
            ModTooltip.REALITY_FORGER_RANGE.format(PeripheralWorksConfig.realityForgerMaxRange),
        )
    }
}
