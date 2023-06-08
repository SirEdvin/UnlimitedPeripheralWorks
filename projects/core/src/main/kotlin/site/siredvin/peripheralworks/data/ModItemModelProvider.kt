package site.siredvin.peripheralworks.data

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.models.ItemModelGenerators
import net.minecraft.data.models.model.ModelTemplate
import net.minecraft.data.models.model.ModelTemplates
import net.minecraft.data.models.model.TextureMapping
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import site.siredvin.peripheralium.xplat.XplatRegistries
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items
import java.util.*

object ModItemModelProvider {
    val TURTLE_LEFT_UPGRADE = ModelTemplate(
        Optional.of(ResourceLocation(PeripheralWorksCore.MOD_ID, "turtle/upgrade_base_left")),
        Optional.empty(),
        TextureSlot.TEXTURE,
    )

    val TURTLE_RIGHT_UPGRADE = ModelTemplate(
        Optional.of(ResourceLocation(PeripheralWorksCore.MOD_ID, "turtle/upgrade_base_right")),
        Optional.empty(),
        TextureSlot.TEXTURE,
    )

    fun turtleUpgrades(generators: ItemModelGenerators, block: Block) {
        TURTLE_RIGHT_UPGRADE.create(
            XplatRegistries.BLOCKS.getKey(block).withPrefix("turtle/").withSuffix("_right"),
            TextureMapping().put(TextureSlot.TEXTURE, TextureMapping.getBlockTexture(block).withSuffix("_side")),
            generators.output
        )
        TURTLE_LEFT_UPGRADE.create(
            XplatRegistries.BLOCKS.getKey(block).withPrefix("turtle/").withSuffix("_left"),
            TextureMapping().put(TextureSlot.TEXTURE, TextureMapping.getBlockTexture(block).withSuffix("_side")),
            generators.output
        )
    }


    fun addModels(generators: ItemModelGenerators) {
        generators.generateFlatItem(Items.PERIPHERALIUM_HUB.get(), ModelTemplates.FLAT_ITEM)
        generators.generateFlatItem(Items.NETHERITE_PERIPHERALIUM_HUB.get(), ModelTemplates.FLAT_ITEM)
        generators.generateFlatItem(Items.ULTIMATE_CONFIGURATOR.get(), ModelTemplates.FLAT_ITEM)

        turtleUpgrades(generators, Blocks.UNIVERSAL_SCANNER.get())
        turtleUpgrades(generators, Blocks.ULTIMATE_SENSOR.get())
    }
}