package site.siredvin.peripheralworks.xplat

import dan200.computercraft.api.client.ComputerCraftAPIClient
import dan200.computercraft.api.client.turtle.TurtleUpgradeModeller
import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import site.siredvin.peripheralworks.PeripheralWorksClientCore
import site.siredvin.peripheralworks.client.turtle.PeripheraliumHubModeller
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.peripheralworks.computercraft.pocket.PeripheraliumHubPocketUpgrade
import site.siredvin.peripheralworks.computercraft.turtles.PeripheraliumHubTurtleUpgrade
import java.util.function.Consumer


object PeripheralWorksCommonHooks {
    private fun registerTurtleUpgrades() {
        PeripheralWorksPlatform.registerTurtleUpgrade(
            PeripheraliumHubPeripheral.ID,

            TurtleUpgradeSerialiser.simpleWithCustomItem { _, stack ->
                return@simpleWithCustomItem PeripheraliumHubTurtleUpgrade(
                    PeripheralWorksConfig::peripheraliumHubUpgradeCount,
                    PeripheraliumHubPeripheral.TYPE,
                    stack
                )
            }, { dataProvider, serializer ->
                dataProvider.simpleWithCustomItem(
                    PeripheraliumHubPeripheral.ID,
                    serializer, Items.PERIPHERALIUM_HUB.get()
                )
            },
            listOf(Consumer {
                PeripheralWorksClientCore.registerHook {
                    ComputerCraftAPIClient.registerTurtleUpgradeModeller(
                        it.get(), PeripheraliumHubModeller(0.5f)
                    )
                }
            })
        )

        PeripheralWorksPlatform.registerTurtleUpgrade(
            PeripheraliumHubPeripheral.NETHERITE_ID,

            TurtleUpgradeSerialiser.simpleWithCustomItem { _, stack ->
                return@simpleWithCustomItem PeripheraliumHubTurtleUpgrade(
                    PeripheralWorksConfig::netheritePeripheraliumHubUpgradeCount,
                    PeripheraliumHubPeripheral.NETHERITE_TYPE,
                    stack
                )
            }, { dataProvider, serializer ->
                dataProvider.simpleWithCustomItem(
                    PeripheraliumHubPeripheral.NETHERITE_ID,
                    serializer, Items.NETHERITE_PERIPHERALIUM_MODEM.get()
                )
            },
            listOf(Consumer {
                PeripheralWorksClientCore.registerHook {
                    ComputerCraftAPIClient.registerTurtleUpgradeModeller(
                        it.get(), PeripheraliumHubModeller(0.5f)
                    )
                }
            })
        )
    }

    private fun registerPocketUpgrades() {
        PeripheralWorksPlatform.registerPocketUpgrade(
            PeripheraliumHubPeripheral.ID,
            PocketUpgradeSerialiser.simpleWithCustomItem { _, stack ->
                return@simpleWithCustomItem PeripheraliumHubPocketUpgrade(
                    PeripheralWorksConfig::peripheraliumHubUpgradeCount,
                    PeripheraliumHubPeripheral.TYPE,
                    stack
                )
            }
        ) { dataProvider, serializer ->
            dataProvider.simpleWithCustomItem(
                PeripheraliumHubPeripheral.ID,
                serializer, Items.PERIPHERALIUM_HUB.get()
            )
        }

        PeripheralWorksPlatform.registerPocketUpgrade(
            PeripheraliumHubPeripheral.NETHERITE_ID,
            PocketUpgradeSerialiser.simpleWithCustomItem { _, stack ->
                return@simpleWithCustomItem PeripheraliumHubPocketUpgrade(
                    PeripheralWorksConfig::netheritePeripheraliumHubUpgradeCount,
                    PeripheraliumHubPeripheral.NETHERITE_TYPE,
                    stack
                )
            }
        ) { dataProvider, serializer ->
            dataProvider.simpleWithCustomItem(
                PeripheraliumHubPeripheral.NETHERITE_ID,
                serializer, Items.NETHERITE_PERIPHERALIUM_MODEM.get()
            )
        }
    }

    fun onRegister() {
        Items.doSomething()
        registerTurtleUpgrades()
        registerPocketUpgrades()
    }
}