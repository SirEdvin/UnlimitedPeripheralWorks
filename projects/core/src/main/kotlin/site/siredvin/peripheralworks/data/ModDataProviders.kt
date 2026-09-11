package site.siredvin.peripheralworks.data

import net.minecraft.data.CachedOutput
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import net.minecraft.data.loot.LootTableProvider
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
        // Forge 1.20.1 cannot condition loot parsing; keep optional items outside the base pack.
        generator.add { output ->
            object : DataProvider {
                private val loot = LootTableProvider(
                    PackOutput(output.outputFolder.resolve("resourcepacks/ae2")),
                    emptySet(),
                    ModLootTableProvider.getTables(integration = true),
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
