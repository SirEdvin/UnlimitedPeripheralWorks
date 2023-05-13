package site.siredvin.peripheralworks.xplat

import dan200.computercraft.api.client.ComputerCraftAPIClient
import dan200.computercraft.api.client.turtle.TurtleUpgradeModeller
import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralium.computercraft.peripheral.owner.PocketPeripheralOwner
import site.siredvin.peripheralium.computercraft.peripheral.owner.TurtlePeripheralOwner
import site.siredvin.peripheralium.computercraft.pocket.BasePocketUpgrade
import site.siredvin.peripheralium.computercraft.pocket.PeripheralPocketUpgrade
import site.siredvin.peripheralium.computercraft.turtle.PeripheralTurtleUpgrade
import site.siredvin.peripheralworks.PeripheralWorksClientCore
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.client.turtle.ScaledItemModeller
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UniversalScannerPeripheral
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
                        it.get(), ScaledItemModeller(0.5f)
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
                        it.get(), ScaledItemModeller(0.5f)
                    )
                }
            })
        )

        PeripheralWorksPlatform.registerTurtleUpgrade(
            UniversalScannerPeripheral.UPGRADE_ID,
            TurtleUpgradeSerialiser.simpleWithCustomItem { _, stack ->
                return@simpleWithCustomItem PeripheralTurtleUpgrade.dynamic(
                    stack.item,
                    UniversalScannerPeripheral::of
                ) { UniversalScannerPeripheral.UPGRADE_ID }
            }, { dataProvider, serializer ->
                dataProvider.simpleWithCustomItem(
                    UniversalScannerPeripheral.UPGRADE_ID,
                    serializer, Blocks.UNIVERSAL_SCANNER.get().asItem()
                )
            } ,
            listOf(Consumer {
                PeripheralWorksClientCore.registerHook {
                    ComputerCraftAPIClient.registerTurtleUpgradeModeller(
                        it.get(), TurtleUpgradeModeller.sided(
                            ResourceLocation(PeripheralWorksCore.MOD_ID, "turtle/universal_scanner_left"),
                            ResourceLocation(PeripheralWorksCore.MOD_ID, "turtle/universal_scanner_right")
                        )
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

        PeripheralWorksPlatform.registerPocketUpgrade(
            UniversalScannerPeripheral.UPGRADE_ID,
            PocketUpgradeSerialiser.simpleWithCustomItem { _, stack ->
                return@simpleWithCustomItem PeripheralPocketUpgrade(
                    UniversalScannerPeripheral.UPGRADE_ID,
                    stack, UniversalScannerPeripheral::of
                )
            }
        ) { dataProvider, serializer ->
            dataProvider.simpleWithCustomItem(
                UniversalScannerPeripheral.UPGRADE_ID,
                serializer, Blocks.UNIVERSAL_SCANNER.get().asItem()
            )
        }
    }

    fun onRegister() {
        BlockEntityTypes.doSomething()
        Items.doSomething()
        Blocks.doSomething()
        registerTurtleUpgrades()
        registerPocketUpgrades()
    }
}