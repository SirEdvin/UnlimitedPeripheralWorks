package site.siredvin.peripheralworks.data

import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.turtle.ITurtleUpgrade
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.data.CachedOutput
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.data.registries.RegistryPatchGenerator
import site.siredvin.broccolium.modules.data.api.GeneratorSink
import site.siredvin.peripheralworks.PeripheralWorksCore

object ModDataProviders {
    fun add(generator: GeneratorSink) {
        generator.add(::ModRecipeProvider)
        generator.addRegistryPatch(PeripheralWorksCore.MOD_ID) { registries ->
            RegistryPatchGenerator.createLookup(
                registries,
                RegistrySetBuilder()
                    .add(ITurtleUpgrade.REGISTRY, ModTurtleUpgradeDataProvider::addUpgrades)
                    .add(IPocketUpgrade.REGISTRY, ModPocketUpgradeDataProvider::addUpgrades),
            )
        }
        generator.lootTable(ModLootTableProvider.getTables())
        // Keep optional item references outside the base pack, as on the 1.20 branch.
        generator.add { output, registries ->
            object : DataProvider {
                private val loot = LootTableProvider(
                    PackOutput(output.outputFolder.resolve("resourcepacks/ae2")),
                    emptySet(),
                    ModLootTableProvider.getTables(integration = true),
                    registries,
                )

                override fun run(output: CachedOutput) = loot.run(output)

                override fun getName() = "AE2 integration loot tables"
            }
        }
        generator.models(ModBlockModelProvider::addModels, ModItemModelProvider::addModels)
        generator.entityTags(PeripheralWorksCore.MOD_ID, ModTagsProvider::entityTypeTags)
        generator.add(::ModEnLanguageProvider)
        generator.add(::ModUaLanguageProvider)
        generator.itemTags(
            PeripheralWorksCore.MOD_ID,
            ModTagsProvider::itemTags,
            generator.blockTags(PeripheralWorksCore.MOD_ID, ModTagsProvider::blockTags),
        )
    }
}
