@file:Suppress("DEPRECATION")

package site.siredvin.peripheralworks.integrations.ae2

// P2P registration and attunement are inspired by Advanced Peripherals' AE2Registries by zyxkad:
// https://github.com/IntelligenceModding/AdvancedPeripherals/blob/fafb3877eed9c40b5a5d56b20421b8625b2d8cce/src/main/java/de/srendi/advancedperipherals/common/addons/ae2/AE2Registries.java

import appeng.api.features.P2PTunnelAttunement
import appeng.api.parts.PartModels
import appeng.core.definitions.AEBlocks
import appeng.core.definitions.AEItems
import appeng.core.definitions.AEParts
import appeng.items.parts.PartItem
import appeng.items.parts.PartModelsHelper
import com.google.gson.JsonObject
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.upgrades.UpgradeType
import net.minecraft.data.models.blockstates.MultiVariantGenerator
import net.minecraft.data.models.blockstates.Variant
import net.minecraft.data.models.blockstates.VariantProperties
import net.minecraft.data.models.model.ModelLocationUtils
import net.minecraft.data.models.model.ModelTemplates
import net.minecraft.data.models.model.TextureMapping
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.entity.BlockEntityType
import site.siredvin.broccolium.modules.base.item.DescriptiveBlockItem
import site.siredvin.broccolium.modules.base.util.BlockUtil
import site.siredvin.broccolium.modules.data.recipe.TweakedShapedRecipeBuilder
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.peripheralworks.PeripheralWorksClientCore
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.client.turtle.ScaledItemModeller
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.data.ModBlockModelProvider
import site.siredvin.peripheralworks.data.ModEnLanguageProvider
import site.siredvin.peripheralworks.data.ModItemModelProvider
import site.siredvin.peripheralworks.data.ModLootTableProvider
import site.siredvin.peripheralworks.data.ModRecipeProvider
import site.siredvin.peripheralworks.data.ModTagsProvider
import site.siredvin.peripheralworks.data.ModUaLanguageProvider
import site.siredvin.peripheralworks.xplat.ModPlatform
import java.util.function.Supplier
import site.siredvin.peripheralworks.common.setup.Items as ModItems

class Registration : Runnable {
    companion object {
        val PATTERN_PEDESTAL = ModPlatform.registerBlock(
            "ae2_pattern_pedestal",
            ::AE2PatternPedestal,
            { DescriptiveBlockItem(it, Item.Properties()) },
        )
        val PATTERN_PEDESTAL_BLOCK_ENTITY: Supplier<BlockEntityType<AE2PatternPedestalBlockEntity>> = ModPlatform.registerBlockEntity(
            ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "ae2_pattern_pedestal"),
        ) {
            PlatformToolkit.get().createBlockEntityType(::AE2PatternPedestalBlockEntity, PATTERN_PEDESTAL.get())
        }

        val ME_NETWORK_PERIPHERAL = ModPlatform.registerBlock(
            "me_network_peripheral",
            { MENetworkPeripheralBlock({ ME_NETWORK_PERIPHERAL_BLOCK_ENTITY.get() }, BlockUtil.defaultProperties()) },
            { DescriptiveBlockItem(it, Item.Properties()) },
        )

        val ME_NETWORK_PERIPHERAL_BLOCK_ENTITY: Supplier<BlockEntityType<MENetworkPeripheralBlockEntity>> = ModPlatform.registerBlockEntity(
            ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "me_network_peripheral"),
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
        PeripheralWorksClientCore.addHook { AE2PatternPedestalClient.register() }
        ModBlockModelProvider.addHook { generators ->
            ModBlockModelProvider.pedestalBlock(generators, PATTERN_PEDESTAL.get(), ResourceLocation.fromNamespaceAndPath("ae2", "block/quartz_block"))
        }
        ModLootTableProvider.addBlockHook { loot, output -> loot.dropSelf(output, PATTERN_PEDESTAL) }
        ModRecipeProvider.addHook { output ->
            TweakedShapedRecipeBuilder(PATTERN_PEDESTAL.get().asItem().defaultInstance)
                .define('P', Blocks.ITEM_PEDESTAL.get())
                .define('B', AEItems.BLANK_PATTERN)
                .pattern("B")
                .pattern("P")
                .save(AE2RecipeConditions.wrap(output))
        }
        ModEnLanguageProvider.addHook { it.add(PATTERN_PEDESTAL.get(), "Pattern pedestal", "§3§oInspect, clear and encode one AE2 pattern at a time. Encoding requires a blank pattern.") }
        ModUaLanguageProvider.addHook { it.add(PATTERN_PEDESTAL.get(), "П'єдестал шаблонів", "§3§oПереглядає, очищає та кодує один шаблон AE2. Кодування потребує порожнього шаблону.") }
        for (id in listOf(
            AE2WirelessTerminalUpgrade.UPGRADE_ID,
            AE2WirelessTerminalUpgrade.CRAFTING_UPGRADE_ID,
        )) {
            lateinit var turtleType: Supplier<UpgradeType<AE2WirelessTerminalUpgrade>>
            turtleType = ModPlatform.registerTurtleUpgrade(
                id,
                UpgradeType.simpleWithCustomItem { stack ->
                    AE2WirelessTerminalUpgrade(id, stack) { turtleType.get() }
                },
            )
            lateinit var pocketType: Supplier<UpgradeType<AE2WirelessTerminalPocketUpgrade>>
            pocketType = ModPlatform.registerPocketUpgrade(
                id,
                UpgradeType.simpleWithCustomItem { stack ->
                    AE2WirelessTerminalPocketUpgrade(id, stack) { pocketType.get() }
                },
            )

            PeripheralWorksClientCore.EXTRA_TURTLE_MODEL_PROVIDERS.add {
                @Suppress("UNCHECKED_CAST")
                Pair(turtleType.get() as UpgradeType<ITurtleUpgrade>, ScaledItemModeller(0.75f, heightShift = 0.15f))
            }
        }
        ModEnLanguageProvider.addHook { it.addTurtle(AE2WirelessTerminalUpgrade.UPGRADE_ID, "AE wireless terminal") }
        ModEnLanguageProvider.addHook { it.addTurtle(AE2WirelessTerminalUpgrade.CRAFTING_UPGRADE_ID, "AE wireless crafting terminal") }
        ModEnLanguageProvider.addHook { it.addPocket(AE2WirelessTerminalUpgrade.UPGRADE_ID, "AE wireless terminal") }
        ModEnLanguageProvider.addHook { it.addPocket(AE2WirelessTerminalUpgrade.CRAFTING_UPGRADE_ID, "AE wireless crafting terminal") }
        ModUaLanguageProvider.addHook { it.addTurtle(AE2WirelessTerminalUpgrade.UPGRADE_ID, "AE бездротова термінальна") }
        ModUaLanguageProvider.addHook { it.addTurtle(AE2WirelessTerminalUpgrade.CRAFTING_UPGRADE_ID, "AE бездротова термінальна крафтингу") }
        ModUaLanguageProvider.addHook { it.addPocket(AE2WirelessTerminalUpgrade.UPGRADE_ID, "AE бездротовий термінал") }
        ModUaLanguageProvider.addHook { it.addPocket(AE2WirelessTerminalUpgrade.CRAFTING_UPGRADE_ID, "AE бездротовий термінал крафтингу") }
        ModRecipeProvider.addHook { output ->
            val conditionalOutput = AE2RecipeConditions.wrap(output)
            TweakedShapedRecipeBuilder(ME_NETWORK_PERIPHERAL.get().asItem().defaultInstance)
                .define('C', Blocks.PERIPHERAL_CASING.get())
                .define('E', AEParts.EXPORT_BUS)
                .define('I', AEBlocks.INTERFACE)
                .define('M', AEParts.IMPORT_BUS)
                .pattern(" E ")
                .pattern("ICI")
                .pattern(" M ")
                .save(conditionalOutput)
        }
        ModLootTableProvider.addBlockHook { loot, output -> loot.dropSelf(output, ME_NETWORK_PERIPHERAL) }
        ModBlockModelProvider.addHook { generators ->
            val model = ModelTemplates.CUBE_ALL.create(
                ME_NETWORK_PERIPHERAL.get(),
                TextureMapping.cube(ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "block/me_network_peripheral")),
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
                ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "part/p2p/wired_network_p2p_tunnel"),
            ) {
                JsonObject().apply {
                    addProperty("parent", "ae2:part/p2p/p2p_tunnel_base")
                    add("textures", JsonObject().apply { addProperty("type", "computercraft:block/wired_modem_face") })
                }
            }
        }
        ModItemModelProvider.addHook { generators ->
            generators.output.accept(
                ModelLocationUtils.getModelLocation(WIRED_NETWORK_P2P_TUNNEL.get()),
            ) {
                JsonObject().apply {
                    addProperty("parent", "ae2:item/p2p_tunnel_base")
                    add("textures", JsonObject().apply { addProperty("type", "computercraft:block/wired_modem_face") })
                }
            }
        }
        ModTagsProvider.addItemHook { tags ->
            tags.tag(P2PTunnelAttunement.getAttunementTag(WIRED_NETWORK_P2P_TUNNEL.get())).add(
                ModItems.ULTIMATE_CONFIGURATOR.get(),
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
