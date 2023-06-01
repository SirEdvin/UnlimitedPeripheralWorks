package site.siredvin.peripheralworks.common.setup

import net.minecraft.world.item.Item
import site.siredvin.peripheralium.common.items.PeripheralItem
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.common.item.UltimateConfigurator
import site.siredvin.peripheralworks.utils.TooltipCollection
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform

object Items {
    val PERIPHERALIUM_HUB = PeripheralWorksPlatform.registerItem("peripheralium_hub") {
        PeripheralItem(
            Item.Properties(),
            PeripheralWorksConfig::enablePeripheraliumHubs,
            alwaysShow = false,
            TooltipCollection::isDisabled,
            TooltipCollection.buildMaxPeripheralsCount(PeripheralWorksConfig::peripheraliumHubUpgradeCount),
        )
    }
    val NETHERITE_PERIPHERALIUM_MODEM = PeripheralWorksPlatform.registerItem("netherite_peripheralium_hub") {
        PeripheralItem(
            Item.Properties(),
            PeripheralWorksConfig::enablePeripheraliumHubs,
            alwaysShow = false,
            TooltipCollection::isDisabled,
            TooltipCollection.buildMaxPeripheralsCount(PeripheralWorksConfig::netheritePeripheraliumHubUpgradeCount),
        )
    }

    val ULTIMATE_CONFIGURATOR = PeripheralWorksPlatform.registerItem("ultimate_configurator", ::UltimateConfigurator)

    fun doSomething() {
    }
}
