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
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
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
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.data.ModBlockModelProvider
import site.siredvin.peripheralworks.data.ModEnLanguageProvider
import site.siredvin.peripheralworks.data.ModItemModelProvider
import site.siredvin.peripheralworks.data.ModLootTableProvider
import site.siredvin.peripheralworks.data.ModRecipeProvider
import site.siredvin.peripheralworks.data.ModTagsProvider
import site.siredvin.peripheralworks.data.ModTurtleUpgradeDataProvider
import site.siredvin.peripheralworks.data.ModUaLanguageProvider
import site.siredvin.peripheralworks.xplat.ModPlatform
import java.util.function.Supplier
import site.siredvin.peripheralworks.common.setup.Items as ModItems

class Registration : Runnable {
    companion object {
        val ME_NETWORK_PERIPHERAL = ModPlatform.registerBlock(
            "me_network_peripheral",
            { MENetworkPeripheralBlock({ ME_NETWORK_PERIPHERAL_BLOCK_ENTITY.get() }, BlockUtil.defaultProperties()) },
            { DescriptiveBlockItem(it, Item.Properties()) },
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
        val wirelessTerminalUpgrade = ModPlatform.registerTurtleUpgrade(
            AE2WirelessTerminalUpgrade.UPGRADE_ID,
            TurtleUpgradeSerialiser.simpleWithCustomItem { id, stack -> AE2WirelessTerminalUpgrade(id, stack) },
        )
        val wirelessCraftingTerminalUpgrade = ModPlatform.registerTurtleUpgrade(
            AE2WirelessTerminalUpgrade.CRAFTING_UPGRADE_ID,
            TurtleUpgradeSerialiser.simpleWithCustomItem { id, stack -> AE2WirelessTerminalUpgrade(id, stack) },
        )
        ModTurtleUpgradeDataProvider.hookUpgrade {
            it.simpleWithCustomItem(AE2WirelessTerminalUpgrade.UPGRADE_ID, wirelessTerminalUpgrade.get(), AEItems.WIRELESS_TERMINAL.asItem()).requireMod("ae2")
        }
        ModTurtleUpgradeDataProvider.hookUpgrade {
            it.simpleWithCustomItem(AE2WirelessTerminalUpgrade.CRAFTING_UPGRADE_ID, wirelessCraftingTerminalUpgrade.get(), AEItems.WIRELESS_CRAFTING_TERMINAL.asItem()).requireMod("ae2")
        }
        PeripheralWorksClientCore.EXTRA_TURTLE_MODEL_PROVIDERS.add {
            @Suppress("UNCHECKED_CAST")
            Pair(wirelessTerminalUpgrade.get() as TurtleUpgradeSerialiser<ITurtleUpgrade>, ScaledItemModeller(0.75f, heightShift = 0.15f))
        }
        PeripheralWorksClientCore.EXTRA_TURTLE_MODEL_PROVIDERS.add {
            @Suppress("UNCHECKED_CAST")
            Pair(wirelessCraftingTerminalUpgrade.get() as TurtleUpgradeSerialiser<ITurtleUpgrade>, ScaledItemModeller(0.75f, heightShift = 0.15f))
        }
        ModEnLanguageProvider.addHook { it.addTurtle(AE2WirelessTerminalUpgrade.UPGRADE_ID, "AE wireless terminal") }
        ModEnLanguageProvider.addHook { it.addTurtle(AE2WirelessTerminalUpgrade.CRAFTING_UPGRADE_ID, "AE wireless crafting terminal") }
        ModUaLanguageProvider.addHook { it.addTurtle(AE2WirelessTerminalUpgrade.UPGRADE_ID, "AE бездротова термінальна") }
        ModUaLanguageProvider.addHook { it.addTurtle(AE2WirelessTerminalUpgrade.CRAFTING_UPGRADE_ID, "AE бездротова термінальна крафтингу") }
        ModRecipeProvider.addHook { output ->
            val conditionalOutput = AE2RecipeConditions.wrap(output)
            TweakedShapedRecipeBuilder.shaped(ME_NETWORK_PERIPHERAL.get())
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
                TextureMapping.cube(ResourceLocation(PeripheralWorksCore.MOD_ID, "block/me_network_peripheral")),
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
