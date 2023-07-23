package site.siredvin.peripheralworks.common.setup

import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import site.siredvin.peripheralium.computercraft.turtle.StatefulPeripheralTurtleUpgrade
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UltimateSensorPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UniversalScannerPeripheral
import site.siredvin.peripheralworks.computercraft.turtles.PeripheraliumHubTurtleUpgrade
import site.siredvin.peripheralworks.xplat.ModPlatform

object TurtleUpgradeSerializers {

    val PERIPHERALIUM_HUB = ModPlatform.registerTurtleUpgrade(
        PeripheraliumHubPeripheral.ID,
        TurtleUpgradeSerialiser.simpleWithCustomItem { _, stack ->
            PeripheraliumHubTurtleUpgrade(
                PeripheralWorksConfig::peripheraliumHubUpgradeCount,
                PeripheraliumHubPeripheral.TYPE,
                stack,
            )
        },
    )

    val NETHERITE_PERIPHERALIUM_HUB = ModPlatform.registerTurtleUpgrade(
        PeripheraliumHubPeripheral.NETHERITE_ID,
        TurtleUpgradeSerialiser.simpleWithCustomItem { _, stack ->
            PeripheraliumHubTurtleUpgrade(
                PeripheralWorksConfig::netheritePeripheraliumHubUpgradeCount,
                PeripheraliumHubPeripheral.NETHERITE_TYPE,
                stack,
            )
        },
    )

    val UNIVERSAL_SCANNER = ModPlatform.registerTurtleUpgrade(
        UniversalScannerPeripheral.UPGRADE_ID,
        TurtleUpgradeSerialiser.simpleWithCustomItem { id, stack ->
            StatefulPeripheralTurtleUpgrade.dynamic(
                stack.item,
                UniversalScannerPeripheral::of,
            ) { id }
        },
    )

    val ULTIMATE_SENSOR = ModPlatform.registerTurtleUpgrade(
        UltimateSensorPeripheral.UPGRADE_ID,
        TurtleUpgradeSerialiser.simpleWithCustomItem { id, stack ->
            StatefulPeripheralTurtleUpgrade.dynamic(
                stack.item,
                UltimateSensorPeripheral::of,
            ) { id }
        },
    )

    fun doSomething() {}
}
