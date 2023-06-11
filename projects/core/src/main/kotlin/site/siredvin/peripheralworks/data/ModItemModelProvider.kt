package site.siredvin.peripheralworks.data

import net.minecraft.data.models.ItemModelGenerators
import net.minecraft.data.models.model.ModelTemplates
import site.siredvin.peripheralium.data.blocks.turtleUpgrades
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items

object ModItemModelProvider {

    fun addModels(generators: ItemModelGenerators) {
        generators.generateFlatItem(Items.PERIPHERALIUM_HUB.get(), ModelTemplates.FLAT_ITEM)
        generators.generateFlatItem(Items.NETHERITE_PERIPHERALIUM_HUB.get(), ModelTemplates.FLAT_ITEM)
        generators.generateFlatItem(Items.ULTIMATE_CONFIGURATOR.get(), ModelTemplates.FLAT_ITEM)

        turtleUpgrades(generators, Blocks.UNIVERSAL_SCANNER.get(), "_side")
        turtleUpgrades(generators, Blocks.ULTIMATE_SENSOR.get(), "_side")
    }
}
