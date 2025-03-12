package site.siredvin.peripheralworks.data

import dan200.computercraft.api.turtle.ITurtleUpgrade
import net.minecraft.Util
import net.minecraft.core.HolderLookup
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.RegistrySetBuilder.PatchedRegistries
import net.minecraft.data.registries.RegistryPatchGenerator
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import site.siredvin.peripheralworks.xplat.ModPlatform
import java.util.concurrent.CompletableFuture

object ModTurtleUpgradeDataProvider {
    fun addUpgrades(upgrades: BootstrapContext<ITurtleUpgrade>) {
        ModPlatform.holder.turtleUpgrades.forEach {
            upgrades.register(
                ResourceKey.create(ITurtleUpgrade.REGISTRY, it.id),
                it.get(),
            )
        }
    }

    // Set up the dynamic registries to contain our turtle upgrades.
    fun makeUpgradeRegistry(registries: CompletableFuture<HolderLookup.Provider>): CompletableFuture<PatchedRegistries> = RegistryPatchGenerator.createLookup(
        registries,
        Util.make(RegistrySetBuilder()) { builder ->
            builder.add(ITurtleUpgrade.REGISTRY, ::addUpgrades)
        },
    )
}
