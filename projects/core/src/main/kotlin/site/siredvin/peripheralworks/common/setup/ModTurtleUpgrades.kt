package site.siredvin.peripheralworks.common.setup

import dan200.computercraft.api.upgrades.UpgradeType
import net.minecraft.world.level.ItemLike
import site.siredvin.broccolium.modules.platform.api.RegistryEntry
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.peripherals.HologramProjectorPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UltimateSensorPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UniversalScannerPeripheral
import site.siredvin.peripheralworks.computercraft.turtles.PeripheraliumHubTurtleUpgrade
import site.siredvin.peripheralworks.computercraft.turtles.UltimateSensorTurtleUpgrade
import site.siredvin.peripheralworks.computercraft.turtles.UniversalScannerTurtleUpgrade
import site.siredvin.peripheralworks.xplat.ModPlatform
import site.siredvin.tweakium.modules.peripheral.owner.TurtlePeripheralOwner
import site.siredvin.tweakium.modules.turtle.StatefulPeripheralTurtleUpgrade
import java.util.function.Supplier

object ModTurtleUpgrades {

    val ITEMS_TO_USE: MutableList<Supplier<out ItemLike>> = mutableListOf(
        Items.NETHERITE_PERIPHERALIUM_HUB,
        Items.PERIPHERALIUM_HUB,
        Blocks.ULTIMATE_SENSOR,
        Blocks.UNIVERSAL_SCANNER,
    )

    val PERIPHERALIUM_HUB = ModPlatform.registerTurtleUpgrade(
        PeripheraliumHubPeripheral.ID,
        UpgradeType.simpleWithCustomItem {
            PeripheraliumHubTurtleUpgrade(
                PeripheralWorksConfig::peripheraliumHubUpgradeCount,
                PeripheraliumHubPeripheral.TYPE,
                it,
            )
        },
    )

    val NETHERITE_PERIPHERALIUM_HUB = ModPlatform.registerTurtleUpgrade(
        PeripheraliumHubPeripheral.NETHERITE_ID,
        UpgradeType.simpleWithCustomItem {
            PeripheraliumHubTurtleUpgrade(
                PeripheralWorksConfig::netheritePeripheraliumHubUpgradeCount,
                PeripheraliumHubPeripheral.NETHERITE_TYPE,
                it,
            )
        },
    )

    val UNIVERSAL_SCANNER: RegistryEntry<UpgradeType<StatefulPeripheralTurtleUpgrade<UniversalScannerPeripheral>>> = ModPlatform.registerTurtleUpgrade(
        UniversalScannerPeripheral.UPGRADE_ID,
        UpgradeType.simpleWithCustomItem(::UniversalScannerTurtleUpgrade),
    )

    val ULTIMATE_SENSOR: RegistryEntry<UpgradeType<StatefulPeripheralTurtleUpgrade<UltimateSensorPeripheral>>> = ModPlatform.registerTurtleUpgrade(
        UltimateSensorPeripheral.UPGRADE_ID,
        UpgradeType.simpleWithCustomItem(::UltimateSensorTurtleUpgrade),
    )

    val HOLOGRAM_PROJECTOR: RegistryEntry<UpgradeType<StatefulPeripheralTurtleUpgrade<HologramProjectorPeripheral>>> = ModPlatform.registerTurtleUpgrade(
        HologramProjectorPeripheral.UPGRADE_ID,
        UpgradeType.simpleWithCustomItem { stack ->
            StatefulPeripheralTurtleUpgrade.dynamic(
                stack.item,
                { turtle, side -> HologramProjectorPeripheral(TurtlePeripheralOwner(turtle, side)) },
                HOLOGRAM_PROJECTOR::get,
            ) { HologramProjectorPeripheral.UPGRADE_ID }
        },
    )

    fun doSomething() {}
}
