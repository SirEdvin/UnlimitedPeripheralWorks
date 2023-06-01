package site.siredvin.peripheralworks.xplat

import dan200.computercraft.api.client.ComputerCraftAPIClient
import dan200.computercraft.api.client.turtle.TurtleUpgradeModeller
import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import site.siredvin.peripheralium.computercraft.pocket.PeripheralPocketUpgrade
import site.siredvin.peripheralium.computercraft.turtle.PeripheralTurtleUpgrade
import site.siredvin.peripheralium.xplat.PeripheraliumPlatform
import site.siredvin.peripheralworks.PeripheralWorksClientCore
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.client.turtle.ScaledItemModeller
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UltimateSensorPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UniversalScannerPeripheral
import site.siredvin.peripheralworks.computercraft.pocket.PeripheraliumHubPocketUpgrade
import site.siredvin.peripheralworks.computercraft.turtles.PeripheraliumHubTurtleUpgrade
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

object PeripheralWorksCommonHooks {

    private val TURTLE_UPGRADES: MutableList<ResourceLocation> = mutableListOf()
    private val POCKET_UPGRADES: MutableList<ResourceLocation> = mutableListOf()

    private fun <T : ITurtleUpgrade, V : Item> registerScaledTurtleUpgrade(id: ResourceLocation, item: Supplier<V>, scaleFactor: Float, builder: Function<ItemStack, T>) {
        TURTLE_UPGRADES.add(id)
        PeripheralWorksPlatform.registerTurtleUpgrade(
            id,
            TurtleUpgradeSerialiser.simpleWithCustomItem { _, stack ->
                return@simpleWithCustomItem builder.apply(stack)
            },
            { dataProvider, serializer ->
                dataProvider.simpleWithCustomItem(
                    id,
                    serializer,
                    item.get(),
                )
            },
            listOf(
                Consumer {
                    PeripheralWorksClientCore.registerHook {
                        ComputerCraftAPIClient.registerTurtleUpgradeModeller(
                            it.get(),
                            ScaledItemModeller(scaleFactor),
                        )
                    }
                },
            ),
        )
    }

    private fun <T : ITurtleUpgrade, V : Block> registerBlockTurtleUpgrade(id: ResourceLocation, block: Supplier<V>, builder: BiFunction<ItemStack, ResourceLocation, T>) {
        TURTLE_UPGRADES.add(id)
        PeripheralWorksPlatform.registerTurtleUpgrade(
            id,
            TurtleUpgradeSerialiser.simpleWithCustomItem { _, stack -> builder.apply(stack, id) },
            { dataProvider, serializer ->
                dataProvider.simpleWithCustomItem(
                    id,
                    serializer,
                    block.get().asItem(),
                )
            },
            listOf(
                Consumer {
                    PeripheralWorksClientCore.registerHook {
                        ComputerCraftAPIClient.registerTurtleUpgradeModeller(
                            it.get(),
                            TurtleUpgradeModeller.sided(
                                ResourceLocation(PeripheralWorksCore.MOD_ID, "turtle/${id.path}_left"),
                                ResourceLocation(PeripheralWorksCore.MOD_ID, "turtle/${id.path}_right"),
                            ),
                        )
                    }
                },
            ),
        )
    }

    private fun <T : IPocketUpgrade, V : Item> registerPocketUpgrade(id: ResourceLocation, item: Supplier<V>, builder: Function<ItemStack, T>) {
        POCKET_UPGRADES.add(id)
        PeripheralWorksPlatform.registerPocketUpgrade(
            id,
            PocketUpgradeSerialiser.simpleWithCustomItem { _, stack -> builder.apply(stack) },
        ) { dataProvider, serializer ->
            dataProvider.simpleWithCustomItem(
                id,
                serializer,
                item.get(),
            )
        }
    }

    private fun <T : IPocketUpgrade, V : Block> registerPocketUpgrade(id: ResourceLocation, block: Supplier<V>, builder: BiFunction<ItemStack, ResourceLocation, T>) {
        POCKET_UPGRADES.add(id)
        PeripheralWorksPlatform.registerPocketUpgrade(
            id,
            PocketUpgradeSerialiser.simpleWithCustomItem { _, stack -> builder.apply(stack, id) },
        ) { dataProvider, serializer ->
            dataProvider.simpleWithCustomItem(
                id,
                serializer,
                block.get().asItem(),
            )
        }
    }

    private fun registerTurtleUpgrades() {
        registerScaledTurtleUpgrade(PeripheraliumHubPeripheral.ID, Items.PERIPHERALIUM_HUB, 0.5f) { stack ->
            PeripheraliumHubTurtleUpgrade(
                PeripheralWorksConfig::peripheraliumHubUpgradeCount,
                PeripheraliumHubPeripheral.TYPE,
                stack,
            )
        }
        registerScaledTurtleUpgrade(PeripheraliumHubPeripheral.NETHERITE_ID, Items.NETHERITE_PERIPHERALIUM_MODEM, 0.5f) { stack ->
            PeripheraliumHubTurtleUpgrade(
                PeripheralWorksConfig::netheritePeripheraliumHubUpgradeCount,
                PeripheraliumHubPeripheral.NETHERITE_TYPE,
                stack,
            )
        }

        registerBlockTurtleUpgrade(UniversalScannerPeripheral.UPGRADE_ID, Blocks.UNIVERSAL_SCANNER) { stack, id ->
            PeripheralTurtleUpgrade.dynamic(
                stack.item,
                UniversalScannerPeripheral::of,
            ) { id }
        }

        registerBlockTurtleUpgrade(UltimateSensorPeripheral.UPGRADE_ID, Blocks.ULTIMATE_SENSOR) { stack, id ->
            PeripheralTurtleUpgrade.dynamic(
                stack.item,
                UltimateSensorPeripheral::of,
            ) { id }
        }
    }

    private fun registerPocketUpgrades() {
        registerPocketUpgrade(PeripheraliumHubPeripheral.ID, Items.PERIPHERALIUM_HUB) {
            PeripheraliumHubPocketUpgrade(
                PeripheralWorksConfig::peripheraliumHubUpgradeCount,
                PeripheraliumHubPeripheral.TYPE,
                it,
            )
        }
        registerPocketUpgrade(PeripheraliumHubPeripheral.NETHERITE_ID, Items.NETHERITE_PERIPHERALIUM_MODEM) {
            PeripheraliumHubPocketUpgrade(
                PeripheralWorksConfig::netheritePeripheraliumHubUpgradeCount,
                PeripheraliumHubPeripheral.NETHERITE_TYPE,
                it,
            )
        }
        registerPocketUpgrade(UniversalScannerPeripheral.UPGRADE_ID, Blocks.UNIVERSAL_SCANNER) { stack, id ->
            PeripheralPocketUpgrade(id, stack, UniversalScannerPeripheral::of)
        }

        registerPocketUpgrade(UltimateSensorPeripheral.UPGRADE_ID, Blocks.ULTIMATE_SENSOR) { stack, id ->
            PeripheralPocketUpgrade(id, stack, UltimateSensorPeripheral::of)
        }
    }

    fun onRegister() {
        BlockEntityTypes.doSomething()
        Items.doSomething()
        Blocks.doSomething()
        registerTurtleUpgrades()
        registerPocketUpgrades()
    }

    fun registerUpgradesInCreativeTab(output: CreativeModeTab.Output) {
        TURTLE_UPGRADES.forEach {
            val upgrade = PeripheraliumPlatform.getTurtleUpgrade(it.toString())
            if (upgrade != null) {
                PeripheraliumPlatform.createTurtlesWithUpgrade(upgrade).forEach(output::accept)
            }
        }
        POCKET_UPGRADES.forEach {
            val upgrade = PeripheraliumPlatform.getPocketUpgrade(it.toString())
            if (upgrade != null) {
                PeripheraliumPlatform.createPocketsWithUpgrade(upgrade).forEach(output::accept)
            }
        }
    }
}
