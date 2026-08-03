@file:Suppress("DEPRECATION")

package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.features.P2PTunnelAttunement
import appeng.api.parts.PartModels
import appeng.core.definitions.AEItems
import appeng.core.definitions.AEParts
import appeng.items.parts.PartItem
import appeng.items.parts.PartModelsHelper
import com.google.gson.JsonObject
import dan200.computercraft.shared.ModRegistry
import net.minecraft.data.models.blockstates.MultiVariantGenerator
import net.minecraft.data.models.blockstates.Variant
import net.minecraft.data.models.blockstates.VariantProperties
import net.minecraft.data.models.model.ModelLocationUtils
import net.minecraft.data.models.model.ModelTemplates
import net.minecraft.data.models.model.TextureMapping
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.block.entity.BlockEntityType
import site.siredvin.broccolium.modules.base.block.GenericBlockEntityBlock
import site.siredvin.broccolium.modules.base.item.HiddenDescriptiveBlockItem
import site.siredvin.broccolium.modules.base.util.BlockUtil
import site.siredvin.broccolium.modules.data.recipe.TweakedShapedRecipeBuilder
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.data.ModBlockModelProvider
import site.siredvin.peripheralworks.data.ModEnLanguageProvider
import site.siredvin.peripheralworks.data.ModItemModelProvider
import site.siredvin.peripheralworks.data.ModLootTableProvider
import site.siredvin.peripheralworks.data.ModRecipeProvider
import site.siredvin.peripheralworks.data.ModTagsProvider
import site.siredvin.peripheralworks.data.ModUaLanguageProvider
import site.siredvin.peripheralworks.utils.TooltipCollection
import site.siredvin.peripheralworks.xplat.ModPlatform
import java.util.function.Supplier

class Registration : Runnable {
    companion object {
        val ME_NETWORK_PERIPHERAL = ModPlatform.registerBlock(
            "me_network_peripheral",
            { GenericBlockEntityBlock({ ME_NETWORK_PERIPHERAL_BLOCK_ENTITY.get() }, false, false, BlockUtil.defaultProperties()) },
            {
                HiddenDescriptiveBlockItem(
                    it,
                    Item.Properties(),
                    Configuration::enableMEInterface,
                    false,
                    TooltipCollection::isDisabled,
                )
            },
        )

        val ME_NETWORK_PERIPHERAL_BLOCK_ENTITY: Supplier<BlockEntityType<MENetworkPeripheralBlockEntity>> = ModPlatform.registerBlockEntity(
            ResourceLocation(PeripheralWorksCore.MOD_ID, "me_network_peripheral"),
        ) {
            PlatformToolkit.get().createBlockEntityType(::MENetworkPeripheralBlockEntity, ME_NETWORK_PERIPHERAL.get())
        }

        val WIRED_NETWORK_P2P_TUNNEL = ModPlatform.registerItem("wired_network_p2p_tunnel") {
            PartItem(Item.Properties(), WiredNetworkP2PTunnelPart::class.java, ::WiredNetworkP2PTunnelPart)
        }

        init {
            PartModels.registerModels(PartModelsHelper.createModels(WiredNetworkP2PTunnelPart::class.java))
        }
    }

    override fun run() {
        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)
        ModRecipeProvider.addHook { output ->
            val conditionalOutput = AE2RecipeConditions.wrap(output)
            TweakedShapedRecipeBuilder.shaped(ME_NETWORK_PERIPHERAL.get())
                .define('C', Blocks.PERIPHERAL_CASING.get())
                .define('F', AEItems.FLUIX_CRYSTAL)
                .define('P', AEItems.ENGINEERING_PROCESSOR)
                .pattern("FPF")
                .pattern("PCP")
                .pattern("FPF")
                .save(conditionalOutput)
            TweakedShapedRecipeBuilder.shaped(WIRED_NETWORK_P2P_TUNNEL.get())
                .define('T', AEParts.ME_P2P_TUNNEL)
                .define('C', Ingredient.of(ModRegistry.Items.CABLE.get()))
                .pattern(" C ")
                .pattern("CTC")
                .pattern(" C ")
                .save(conditionalOutput)
        }
        ModLootTableProvider.addBlockHook { loot, output -> loot.dropSelf(output, ME_NETWORK_PERIPHERAL) }
        ModBlockModelProvider.addHook { generators ->
            val model = ModelTemplates.CUBE_ALL.create(
                ME_NETWORK_PERIPHERAL.get(),
                TextureMapping.cube(ResourceLocation("ae2", "block/fluix_block")),
                generators.modelOutput,
            )
            generators.blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(
                    ME_NETWORK_PERIPHERAL.get(),
                    Variant.variant().with(VariantProperties.MODEL, model),
                ),
            )
            generators.delegateItemModel(ME_NETWORK_PERIPHERAL.get(), model)
            generators.modelOutput.accept(
                ResourceLocation(PeripheralWorksCore.MOD_ID, "part/p2p/wired_network_p2p_tunnel"),
            ) {
                JsonObject().apply {
                    addProperty("parent", "ae2:part/p2p/p2p_tunnel_base")
                    add("textures", JsonObject().apply { addProperty("type", "computercraft:block/cable_side") })
                }
            }
        }
        ModItemModelProvider.addHook { generators ->
            ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(WIRED_NETWORK_P2P_TUNNEL.get()),
                TextureMapping.layer0(ResourceLocation("computercraft", "block/cable_side")),
                generators.output,
            )
        }
        ModTagsProvider.addItemHook { tags ->
            tags.tag(P2PTunnelAttunement.getAttunementTag(WIRED_NETWORK_P2P_TUNNEL.get())).add(
                ModRegistry.Items.CABLE.get(),
                ModRegistry.Items.WIRED_MODEM.get(),
                ModRegistry.Items.WIRED_MODEM_FULL.get(),
            )
        }
        ModEnLanguageProvider.addHook {
            it.add(ME_NETWORK_PERIPHERAL.get(), "ME network peripheral", "§3§oExposes this AE2 network to computers and consumes one channel")
            it.add(WIRED_NETWORK_P2P_TUNNEL.get(), "Wired network P2P tunnel", "§3§oCarries a CC:Tweaked wired network through an AE2 P2P link")
        }
        ModUaLanguageProvider.addHook {
            it.add(ME_NETWORK_PERIPHERAL.get(), "Периферія мережі ME", "§3§oНадає комп'ютерам доступ до мережі AE2 та використовує один канал")
            it.add(WIRED_NETWORK_P2P_TUNNEL.get(), "P2P-тунель провідної мережі", "§3§oПередає провідну мережу CC:Tweaked через P2P-з'єднання AE2")
        }
    }
}
