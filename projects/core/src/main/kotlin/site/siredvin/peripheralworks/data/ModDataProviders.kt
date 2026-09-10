package site.siredvin.peripheralworks.data

import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.turtle.ITurtleUpgrade
import net.minecraft.core.RegistrySetBuilder
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
