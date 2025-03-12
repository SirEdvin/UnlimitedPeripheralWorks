package site.siredvin.peripheralworks.data

import site.siredvin.broccolium.modules.data.api.GeneratorSink
import site.siredvin.peripheralworks.PeripheralWorksCore

object ModDataProviders {
    fun add(generator: GeneratorSink) {
        generator.add {
            ModRecipeProvider(it)
        }
        generator.add {
            ModPocketUpgradeDataProvider(it)
        }
        generator.add {
            ModTurtleUpgradeDataProvider(it)
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
