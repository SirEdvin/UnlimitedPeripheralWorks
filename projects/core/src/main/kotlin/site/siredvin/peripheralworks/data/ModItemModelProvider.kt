package site.siredvin.peripheralworks.data

import net.minecraft.data.models.ItemModelGenerators
import net.minecraft.data.models.model.ModelLocationUtils
import net.minecraft.data.models.model.ModelTemplates
import net.minecraft.data.models.model.TextureMapping
import site.siredvin.peripheralium.data.blocks.turtleUpgrades
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items

object ModItemModelProvider {

    fun addModels(generators: ItemModelGenerators) {
        generators.generateFlatItem(Items.PERIPHERALIUM_HUB.get(), ModelTemplates.FLAT_ITEM)
        generators.generateFlatItem(Items.NETHERITE_PERIPHERALIUM_HUB.get(), ModelTemplates.FLAT_ITEM)
        generators.generateFlatItem(Items.ULTIMATE_CONFIGURATOR.get(), ModelTemplates.FLAT_ITEM)
        generators.generateFlatItem(Items.ANALYZER.get(), ModelTemplates.FLAT_ITEM)
        ModelTemplates.FLAT_ITEM.create(
            ModelLocationUtils.getModelLocation(Items.ENTITY_CARD.get()).withSuffix("_empty"),
            TextureMapping.layer0(Items.ENTITY_CARD.get()),
            generators.output,
        )
        ModelTemplates.FLAT_ITEM.create(
            ModelLocationUtils.getModelLocation(Items.ENTITY_CARD.get()).withSuffix("_active"),
            TextureMapping.layer0(TextureMapping.getItemTexture(Items.ENTITY_CARD.get()).withSuffix("_active")),
            generators.output,
        )

        turtleUpgrades(generators, Blocks.UNIVERSAL_SCANNER.get(), "_side")
        turtleUpgrades(generators, Blocks.ULTIMATE_SENSOR.get(), "_side")
    }
}
