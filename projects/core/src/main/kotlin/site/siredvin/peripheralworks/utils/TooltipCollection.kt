package site.siredvin.peripheralworks.utils

import net.minecraft.network.chat.Component
import site.siredvin.broccolium.modules.base.item.HiddenDescriptiveBlockItem
import site.siredvin.broccolium.modules.base.item.HiddenDescriptiveItemItem
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.operations.SphereOperations
import site.siredvin.peripheralworks.data.ModTooltip
import java.util.function.Function
import java.util.function.Supplier

object TooltipCollection {
    fun isDisabled(item: HiddenDescriptiveItemItem): List<Component> {
        if (item.isEnabled()) {
            return emptyList()
        }
        return listOf(ModTooltip.ITEM_DISABLED.text)
    }

    fun isDisabled(item: HiddenDescriptiveBlockItem): List<Component> {
        if (item.isEnabled()) {
            return emptyList()
        }
        return listOf(ModTooltip.ITEM_DISABLED.text)
    }

    fun buildMaxPeripheralsCount(maxCountSup: Supplier<Int>): Function<HiddenDescriptiveItemItem, List<Component>> = Function { listOf(ModTooltip.PERIPHERALIUM_HUB_MAX_PERIPHERALS.format(maxCountSup.get())) }

    fun universalScanningRadius(@Suppress("UNUSED_PARAMETER") item: HiddenDescriptiveBlockItem): List<Component> = listOf(
        ModTooltip.UNIVERSAL_SCANNER_FREE_RANGE.format(SphereOperations.PORTABLE_UNIVERSAL_SCAN.maxFreeRadius),
        ModTooltip.UNIVERSAL_SCANNER_MAX_RANGE.format(SphereOperations.PORTABLE_UNIVERSAL_SCAN.maxCostRadius),
    )

    fun remoteObserverTooptips(@Suppress("UNUSED_PARAMETER") item: HiddenDescriptiveBlockItem): List<Component> = listOf(
        ModTooltip.REMOTE_OBSERVER_RANGE.format(PeripheralWorksConfig.remoteObserverMaxRange),
        ModTooltip.REMOTE_OBSERVER_MAX_CAPACITY.format(PeripheralWorksConfig.remoteObserverMaxCapacity),
    )

    fun peripheralProxyTooptips(@Suppress("UNUSED_PARAMETER") item: HiddenDescriptiveBlockItem): List<Component> = listOf(
        ModTooltip.PERIPHERAL_PROXY_RANGE.format(PeripheralWorksConfig.peripheralProxyMaxRange),
        ModTooltip.PERIPHERAL_PROXY_MAX_CAPACITY.format(PeripheralWorksConfig.peripheralProxyMaxCapacity),
    )

    fun realityForgerTooptips(@Suppress("UNUSED_PARAMETER") item: HiddenDescriptiveBlockItem): List<Component> = listOf(
        ModTooltip.REALITY_FORGER_RANGE.format(PeripheralWorksConfig.realityForgerMaxRange),
    )
}
