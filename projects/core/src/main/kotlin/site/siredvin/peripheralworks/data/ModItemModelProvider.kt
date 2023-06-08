package site.siredvin.peripheralworks.data

import net.minecraft.data.models.ItemModelGenerators
import net.minecraft.data.models.model.ModelTemplates
import site.siredvin.peripheralworks.common.setup.Items

object ModItemModelProvider {
    fun addModels(generators: ItemModelGenerators) {
        generators.generateFlatItem(Items.PERIPHERALIUM_HUB.get(), ModelTemplates.FLAT_ITEM)
        generators.generateFlatItem(Items.NETHERITE_PERIPHERALIUM_HUB.get(), ModelTemplates.FLAT_ITEM)
        generators.generateFlatItem(Items.ULTIMATE_CONFIGURATOR.get(), ModelTemplates.FLAT_ITEM)
    }
}