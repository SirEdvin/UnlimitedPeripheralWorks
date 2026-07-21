package site.siredvin.peripheralworks

import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.upgrades.UpgradeType
import net.minecraft.advancements.CriterionTrigger
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NewRegistryEvent
import site.siredvin.broccolium.modules.base.ForgeIntegrationLoader
import site.siredvin.peripheralium.ForgePeripheralium
import site.siredvin.peripheralworks.common.configuration.ConfigHolder
import site.siredvin.peripheralworks.forge.ForgeCommonHooks
import site.siredvin.peripheralworks.forge.ForgeModBlocksReference
import site.siredvin.peripheralworks.forge.ForgeModPlatform
import site.siredvin.peripheralworks.forge.ForgeModRecipeIngredients
import site.siredvin.peripheralworks.forge.ForgeNetworkHandler
import site.siredvin.peripheralworks.subsystem.recipe.ForgeRecipeTransformers
import site.siredvin.peripheralworks.xplat.PeripheralWorksCommonHooks

@Mod(PeripheralWorksCore.MOD_ID)
class ForgePeripheralWorks(modEventBus: IEventBus, modContainer: ModContainer) {

    companion object {
        val blocksRegistry: DeferredRegister<Block> =
            DeferredRegister.create(BuiltInRegistries.BLOCK, PeripheralWorksCore.MOD_ID)
        val itemsRegistry: DeferredRegister<Item> =
            DeferredRegister.create(BuiltInRegistries.ITEM, PeripheralWorksCore.MOD_ID)
        val blockEntityTypesRegistry: DeferredRegister<BlockEntityType<*>> =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, PeripheralWorksCore.MOD_ID)
        val creativeTabRegistry: DeferredRegister<CreativeModeTab> =
            DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), PeripheralWorksCore.MOD_ID)
        val recipeSerializers: DeferredRegister<RecipeSerializer<*>> =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, PeripheralWorksCore.MOD_ID)
        val criterionTriggers: DeferredRegister<CriterionTrigger<*>> =
            DeferredRegister.create(BuiltInRegistries.TRIGGER_TYPES, PeripheralWorksCore.MOD_ID)
        val dataComponentTypes: DeferredRegister<DataComponentType<*>> =
            DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, PeripheralWorksCore.MOD_ID)
        val turtleUpgradeTypes: DeferredRegister<UpgradeType<out ITurtleUpgrade>> =
            DeferredRegister.create(ITurtleUpgrade.typeRegistry(), PeripheralWorksCore.MOD_ID)
        val pocketUpgradeTypes: DeferredRegister<UpgradeType<out IPocketUpgrade>> =
            DeferredRegister.create(IPocketUpgrade.typeRegistry(), PeripheralWorksCore.MOD_ID)

        val loader = ForgeIntegrationLoader(
            ForgePeripheralWorks::class.java.getPackage().name,
            PeripheralWorksCore.logger,
        )
    }

    init {
        ForgePeripheralium.sayHi()
        // Configure configuration
        modContainer.registerConfig(ModConfig.Type.COMMON, ConfigHolder.commonSpec, "${PeripheralWorksCore.MOD_ID}.toml")
        PeripheralWorksCore.configure(ForgeModPlatform, ForgeModRecipeIngredients, ForgeModBlocksReference)
        modEventBus.addListener(this::commonSetup)
        modEventBus.addListener(this::registrySetup)
        modEventBus.addListener(ForgeNetworkHandler::setup)
        modEventBus.addListener(ForgeCommonHooks::registerCapabilities)
        // Register items and blocks
        PeripheralWorksCommonHooks.onRegister()
        blocksRegistry.register(modEventBus)
        itemsRegistry.register(modEventBus)
        blockEntityTypesRegistry.register(modEventBus)
        creativeTabRegistry.register(modEventBus)
        recipeSerializers.register(modEventBus)
        criterionTriggers.register(modEventBus)
        dataComponentTypes.register(modEventBus)
        turtleUpgradeTypes.register(modEventBus)
        pocketUpgradeTypes.register(modEventBus)

        ForgeRecipeTransformers.init()
    }

    @Suppress("UNUSED_PARAMETER")
    fun commonSetup(event: FMLCommonSetupEvent) {
        // Load all integrations
        loader.maybeLoadIntegration("additionallanterns").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("occultism").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("easy_villagers").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("toms_storage").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("ae2").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("mna").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("deepresonance").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("powah").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("automobility").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("fluxnetworks").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("create").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("embers").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("theurgy").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("emi").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("gtceu").ifPresent { (it as Runnable).run() }
        PeripheralWorksCommonHooks.afterConfigurationLoaded()
    }

    @Suppress("UNUSED_PARAMETER")
    fun registrySetup(event: NewRegistryEvent) {
        loader.maybeLoadIntegration("integrateddynamics").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("naturescompass").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("ars_nouveau").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("projecte").ifPresent { (it as Runnable).run() }
    }
}
