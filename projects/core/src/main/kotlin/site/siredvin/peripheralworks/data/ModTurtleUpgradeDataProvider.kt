package site.siredvin.peripheralworks.data

import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.upgrades.UpgradeType
import net.minecraft.Util
import net.minecraft.core.HolderLookup
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.RegistrySetBuilder.PatchedRegistries
import net.minecraft.data.registries.RegistryPatchGenerator
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.common.setup.ModTurtleUpgrades
import site.siredvin.peripheralworks.computercraft.peripherals.HologramProjectorPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.peripheralworks.computercraft.turtles.PeripheraliumHubTurtleUpgrade
import site.siredvin.peripheralworks.computercraft.turtles.UltimateSensorTurtleUpgrade
import site.siredvin.peripheralworks.computercraft.turtles.UniversalScannerTurtleUpgrade
import site.siredvin.tweakium.modules.peripheral.owner.TurtlePeripheralOwner
import site.siredvin.tweakium.modules.turtle.StatefulPeripheralTurtleUpgrade
import java.util.concurrent.CompletableFuture

object ModTurtleUpgradeDataProvider {
    fun addUpgrades(upgrades: BootstrapContext<ITurtleUpgrade>) {
        upgrades.register(
            ResourceKey.create(ITurtleUpgrade.REGISTRY, ModTurtleUpgrades.PERIPHERALIUM_HUB.id),
            PeripheraliumHubTurtleUpgrade(
                PeripheralWorksConfig::peripheraliumHubUpgradeCount,
                PeripheraliumHubPeripheral.TYPE,
                Items.PERIPHERALIUM_HUB.get().defaultInstance,
            ),
        )
        upgrades.register(
            ResourceKey.create(ITurtleUpgrade.REGISTRY, ModTurtleUpgrades.NETHERITE_PERIPHERALIUM_HUB.id),
            PeripheraliumHubTurtleUpgrade(
                PeripheralWorksConfig::netheritePeripheraliumHubUpgradeCount,
                PeripheraliumHubPeripheral.NETHERITE_TYPE,
                Items.NETHERITE_PERIPHERALIUM_HUB.get().defaultInstance,
            ),
        )
        upgrades.register(
            ResourceKey.create(ITurtleUpgrade.REGISTRY, ModTurtleUpgrades.UNIVERSAL_SCANNER.id),
            UniversalScannerTurtleUpgrade(Blocks.UNIVERSAL_SCANNER.get().asItem().defaultInstance),
        )
        upgrades.register(
            ResourceKey.create(ITurtleUpgrade.REGISTRY, ModTurtleUpgrades.ULTIMATE_SENSOR.id),
            UltimateSensorTurtleUpgrade(Blocks.ULTIMATE_SENSOR.get().asItem().defaultInstance),
        )
        upgrades.register(
            ResourceKey.create(ITurtleUpgrade.REGISTRY, ModTurtleUpgrades.HOLOGRAM_PROJECTOR.id),
            StatefulPeripheralTurtleUpgrade.dynamic(
                Blocks.HOLOGRAM_PROJECTOR.get().asItem(),
                { turtle, side -> HologramProjectorPeripheral(TurtlePeripheralOwner(turtle, side)) },
                {
                    @Suppress("UNCHECKED_CAST")
                    ModTurtleUpgrades.HOLOGRAM_PROJECTOR.get() as UpgradeType<StatefulPeripheralTurtleUpgrade<HologramProjectorPeripheral>>
                },
                { HologramProjectorPeripheral.UPGRADE_ID },
            ),
        )
    }

    // Set up the dynamic registries to contain our turtle upgrades.
    fun makeUpgradeRegistry(registries: CompletableFuture<HolderLookup.Provider>): CompletableFuture<PatchedRegistries> = RegistryPatchGenerator.createLookup(
        registries,
        Util.make(RegistrySetBuilder()) { builder ->
            builder.add(ITurtleUpgrade.REGISTRY, ::addUpgrades)
        },
    )
}
