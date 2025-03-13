package site.siredvin.peripheralworks.data

import dan200.computercraft.api.pocket.IPocketUpgrade
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
import site.siredvin.peripheralworks.common.setup.ModPocketUpgrades
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.peripheralworks.computercraft.pocket.PeripheraliumHubPocketUpgrade
import site.siredvin.peripheralworks.computercraft.pocket.UltimateSensorPocketUpgrade
import site.siredvin.peripheralworks.computercraft.pocket.UniversalScannerPocketUpgrade
import java.util.concurrent.CompletableFuture

object ModPocketUpgradeDataProvider {
    fun addUpgrades(upgrades: BootstrapContext<IPocketUpgrade>) {
        upgrades.register(
            ResourceKey.create(IPocketUpgrade.REGISTRY, ModPocketUpgrades.PERIPHERALIUM_HUB.id),
            PeripheraliumHubPocketUpgrade(
                PeripheralWorksConfig::peripheraliumHubUpgradeCount,
                PeripheraliumHubPeripheral.TYPE,
                Items.PERIPHERALIUM_HUB.get().defaultInstance,
            ),
        )
        upgrades.register(
            ResourceKey.create(IPocketUpgrade.REGISTRY, ModPocketUpgrades.NETHERITE_PERIPHERALIUM_HUB.id),
            PeripheraliumHubPocketUpgrade(
                PeripheralWorksConfig::peripheraliumHubUpgradeCount,
                PeripheraliumHubPeripheral.TYPE,
                Items.NETHERITE_PERIPHERALIUM_HUB.get().defaultInstance,
            ),
        )
        upgrades.register(
            ResourceKey.create(IPocketUpgrade.REGISTRY, ModPocketUpgrades.UNIVERSAL_SCANNER.id),
            UniversalScannerPocketUpgrade(Blocks.UNIVERSAL_SCANNER.get().asItem().defaultInstance),
        )
        upgrades.register(
            ResourceKey.create(IPocketUpgrade.REGISTRY, ModPocketUpgrades.ULTIMATE_SENSOR.id),
            UltimateSensorPocketUpgrade(Blocks.ULTIMATE_SENSOR.get().asItem().defaultInstance),
        )
    }

    // Set up the dynamic registries to contain our turtle upgrades.
    fun makeUpgradeRegistry(registries: CompletableFuture<HolderLookup.Provider>): CompletableFuture<PatchedRegistries> = RegistryPatchGenerator.createLookup(
        registries,
        Util.make(RegistrySetBuilder()) { builder ->
            builder.add(IPocketUpgrade.REGISTRY, ::addUpgrades)
        },
    )
}
