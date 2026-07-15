package site.siredvin.peripheralworks.common.setup

import dan200.computercraft.api.upgrades.UpgradeType
import net.minecraft.world.level.ItemLike
import site.siredvin.broccolium.modules.platform.api.RegistryEntry
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.peripherals.HologramProjectorPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UltimateSensorPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UniversalScannerPeripheral
import site.siredvin.peripheralworks.computercraft.pocket.PeripheraliumHubPocketUpgrade
import site.siredvin.peripheralworks.computercraft.pocket.UltimateSensorPocketUpgrade
import site.siredvin.peripheralworks.computercraft.pocket.UniversalScannerPocketUpgrade
import site.siredvin.peripheralworks.xplat.ModPlatform
import site.siredvin.tweakium.modules.peripheral.owner.PocketPeripheralOwner
import site.siredvin.tweakium.modules.pocket.StatefulPeripheralPocketUpgrade
import java.util.function.Supplier

object ModPocketUpgrades {

    val ITEMS_TO_USE: MutableList<Supplier<out ItemLike>> = mutableListOf(
        Items.NETHERITE_PERIPHERALIUM_HUB,
        Items.PERIPHERALIUM_HUB,
        Blocks.ULTIMATE_SENSOR,
        Blocks.UNIVERSAL_SCANNER,
    )

    val PERIPHERALIUM_HUB = ModPlatform.registerPocketUpgrade(
        PeripheraliumHubPeripheral.ID,
        UpgradeType.simpleWithCustomItem {
            PeripheraliumHubPocketUpgrade(
                PeripheralWorksConfig::peripheraliumHubUpgradeCount,
                PeripheraliumHubPeripheral.TYPE,
                it,
            )
        },
    )

    val NETHERITE_PERIPHERALIUM_HUB = ModPlatform.registerPocketUpgrade(
        PeripheraliumHubPeripheral.NETHERITE_ID,
        UpgradeType.simpleWithCustomItem {
            PeripheraliumHubPocketUpgrade(
                PeripheralWorksConfig::netheritePeripheraliumHubUpgradeCount,
                PeripheraliumHubPeripheral.NETHERITE_TYPE,
                it,
            )
        },
    )

    val UNIVERSAL_SCANNER = ModPlatform.registerPocketUpgrade(
        UniversalScannerPeripheral.UPGRADE_ID,
        UpgradeType.simpleWithCustomItem(::UniversalScannerPocketUpgrade),
    )

    val ULTIMATE_SENSOR = ModPlatform.registerPocketUpgrade(
        UltimateSensorPeripheral.UPGRADE_ID,
        UpgradeType.simpleWithCustomItem(::UltimateSensorPocketUpgrade),
    )

    val HOLOGRAM_PROJECTOR: RegistryEntry<UpgradeType<StatefulPeripheralPocketUpgrade<HologramProjectorPeripheral>>> = ModPlatform.registerPocketUpgrade(
        HologramProjectorPeripheral.UPGRADE_ID,
        UpgradeType.simpleWithCustomItem { stack ->
            StatefulPeripheralPocketUpgrade(
                HologramProjectorPeripheral.UPGRADE_ID,
                stack,
                { HologramProjectorPeripheral(PocketPeripheralOwner(it)) },
                {
                    @Suppress("UNCHECKED_CAST")
                    HOLOGRAM_PROJECTOR.get() as UpgradeType<StatefulPeripheralPocketUpgrade<HologramProjectorPeripheral>>
                },
            )
        },
    )

    fun doSomething() {}
}
