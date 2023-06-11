package site.siredvin.peripheralworks.common.setup

import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import site.siredvin.peripheralium.computercraft.pocket.PeripheralPocketUpgrade
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UltimateSensorPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UniversalScannerPeripheral
import site.siredvin.peripheralworks.computercraft.pocket.PeripheraliumHubPocketUpgrade
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform

object PocketUpgradeSerializers {

    val PERIPHERALIUM_HUB = PeripheralWorksPlatform.registerPocketUpgrade(
        PeripheraliumHubPeripheral.ID,
        PocketUpgradeSerialiser.simpleWithCustomItem { _, stack ->
            PeripheraliumHubPocketUpgrade(
                PeripheralWorksConfig::peripheraliumHubUpgradeCount,
                PeripheraliumHubPeripheral.TYPE,
                stack,
            )
        }
    )

    val NETHERITE_PERIPHERALIUM_HUB = PeripheralWorksPlatform.registerPocketUpgrade(
        PeripheraliumHubPeripheral.NETHERITE_ID,
        PocketUpgradeSerialiser.simpleWithCustomItem {_, stack ->
            PeripheraliumHubPocketUpgrade(
                PeripheralWorksConfig::netheritePeripheraliumHubUpgradeCount,
                PeripheraliumHubPeripheral.NETHERITE_TYPE,
                stack,
            )
        }
    )

    val UNIVERSAL_SCANNER = PeripheralWorksPlatform.registerPocketUpgrade(
        UniversalScannerPeripheral.UPGRADE_ID,
        PocketUpgradeSerialiser.simpleWithCustomItem {id, stack ->
            PeripheralPocketUpgrade(id, stack, UniversalScannerPeripheral::of)
        }
    )

    val ULTIMATE_SENSOR = PeripheralWorksPlatform.registerPocketUpgrade(
        UltimateSensorPeripheral.UPGRADE_ID,
        PocketUpgradeSerialiser.simpleWithCustomItem {id, stack ->
            PeripheralPocketUpgrade(id, stack, UltimateSensorPeripheral::of)
        }
    )

    fun doSomething() {}
}