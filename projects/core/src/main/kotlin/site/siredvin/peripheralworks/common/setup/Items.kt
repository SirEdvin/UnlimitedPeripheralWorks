package site.siredvin.peripheralworks.common.setup

import net.minecraft.world.item.Item
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.common.item.Analyzer
import site.siredvin.peripheralworks.common.item.PeripheraliumHub
import site.siredvin.peripheralworks.common.item.UltimateConfigurator
import site.siredvin.peripheralworks.utils.TooltipCollection
import site.siredvin.peripheralworks.xplat.ModPlatform

object Items {
    val PERIPHERALIUM_HUB = ModPlatform.registerItem("peripheralium_hub") {
        PeripheraliumHub(
            Item.Properties(),
            PeripheralWorksConfig::enablePeripheraliumHubs,
            alwaysShow = false,
            TooltipCollection::isDisabled,
            TooltipCollection.buildMaxPeripheralsCount(PeripheralWorksConfig::peripheraliumHubUpgradeCount),
        )
    }
    val NETHERITE_PERIPHERALIUM_HUB = ModPlatform.registerItem("netherite_peripheralium_hub") {
        PeripheraliumHub(
            Item.Properties(),
            PeripheralWorksConfig::enablePeripheraliumHubs,
            alwaysShow = false,
            TooltipCollection::isDisabled,
            TooltipCollection.buildMaxPeripheralsCount(PeripheralWorksConfig::netheritePeripheraliumHubUpgradeCount),
        )
    }

    val ULTIMATE_CONFIGURATOR = ModPlatform.registerItem("ultimate_configurator", ::UltimateConfigurator)

    val ANALYZER = ModPlatform.registerItem("analyzer", ::Analyzer)

    fun doSomething() {
    }
}
