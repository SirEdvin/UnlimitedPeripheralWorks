package site.siredvin.peripheralworks.common.setup

import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UltimateSensorPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UniversalScannerPeripheral
import site.siredvin.peripheralworks.computercraft.turtles.PeripheraliumHubTurtleUpgrade
import site.siredvin.peripheralworks.xplat.ModPlatform
import site.siredvin.tweakium.modules.turtle.StatefulPeripheralTurtleUpgrade

object ModTurtleUpgrades {

    val PERIPHERALIUM_HUB = ModPlatform.registerTurtleUpgrade(
        PeripheraliumHubPeripheral.ID,
        PeripheraliumHubTurtleUpgrade(
            PeripheralWorksConfig::peripheraliumHubUpgradeCount,
            PeripheraliumHubPeripheral.TYPE,
            Items.PERIPHERALIUM_HUB.get().defaultInstance,
        ),
    )

    val NETHERITE_PERIPHERALIUM_HUB = ModPlatform.registerTurtleUpgrade(
        PeripheraliumHubPeripheral.NETHERITE_ID,
        PeripheraliumHubTurtleUpgrade(
            PeripheralWorksConfig::netheritePeripheraliumHubUpgradeCount,
            PeripheraliumHubPeripheral.NETHERITE_TYPE,
            Items.NETHERITE_PERIPHERALIUM_HUB.get().defaultInstance,
        ),
    )

    val UNIVERSAL_SCANNER = ModPlatform.registerTurtleUpgrade(
        UniversalScannerPeripheral.UPGRADE_ID,
        StatefulPeripheralTurtleUpgrade.dynamic(
            Blocks.UNIVERSAL_SCANNER.get().asItem(),
            UniversalScannerPeripheral::of,
        ) { UniversalScannerPeripheral.UPGRADE_ID },
    )

    val ULTIMATE_SENSOR = ModPlatform.registerTurtleUpgrade(
        UltimateSensorPeripheral.UPGRADE_ID,
        StatefulPeripheralTurtleUpgrade.dynamic(
            Blocks.ULTIMATE_SENSOR.get().asItem(),
            UltimateSensorPeripheral::of,
        ) { UltimateSensorPeripheral.UPGRADE_ID },
    )

    fun doSomething() {}
}
