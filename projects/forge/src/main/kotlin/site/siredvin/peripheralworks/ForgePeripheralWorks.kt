package site.siredvin.peripheralworks

import dan200.computercraft.api.ForgeComputerCraftAPI
import dan200.computercraft.api.peripheral.IPeripheralProvider
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.NewRegistryEvent
import site.siredvin.peripheralium.ForgePeripheralium
import site.siredvin.peripheralworks.common.Registries
import site.siredvin.peripheralworks.common.configuration.ConfigHolder
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.computercraft.EnergyStorageProvider
import site.siredvin.peripheralworks.computercraft.FluidStorageProvider
import site.siredvin.peripheralworks.forge.ForgeModRecipeIngredients
import site.siredvin.peripheralworks.forge.ForgePeripheralWorksPlatform
import site.siredvin.peripheralworks.utils.Platform
import site.siredvin.peripheralworks.xplat.PeripheralWorksCommonHooks
import thedarkcolour.kotlinforforge.forge.MOD_CONTEXT


@Mod(PeripheralWorksCore.MOD_ID)
object ForgePeripheralWorks {

    val blocksRegistry: DeferredRegister<Block> =
        DeferredRegister.create(ForgeRegistries.BLOCKS, PeripheralWorksCore.MOD_ID)
    val itemsRegistry: DeferredRegister<Item> =
        DeferredRegister.create(ForgeRegistries.ITEMS, PeripheralWorksCore.MOD_ID)

    init {
        ForgePeripheralium.sayHi()
        // Configure configuration
        val context = ModLoadingContext.get()
        context.registerConfig(ModConfig.Type.COMMON, ConfigHolder.COMMON_SPEC, "${PeripheralWorksCore.MOD_ID}.toml")

        PeripheralWorksCore.configure(ForgePeripheralWorksPlatform, ForgeModRecipeIngredients)
        ComputerCraftProxy.addProvider(FluidStorageProvider)
        ComputerCraftProxy.addProvider(EnergyStorageProvider)
        val eventBus = MOD_CONTEXT.getKEventBus()
        eventBus.addListener(this::commonSetup)
        eventBus.addListener(this::registrySetup)
        // Register items and blocks
        PeripheralWorksCommonHooks.onRegister()
        blocksRegistry.register(eventBus)
        itemsRegistry.register(eventBus)
        Registries.TURTLE_SERIALIZER.register(eventBus)
        Registries.POCKET_SERIALIZER.register(eventBus)
    }

    fun commonSetup(event: FMLCommonSetupEvent) {
        // Load all integrations
        Platform.maybeLoadIntegration("occultism").ifPresent { (it as Runnable).run() }
        Platform.maybeLoadIntegration("easy_villagers").ifPresent { (it as Runnable).run() }
        Platform.maybeLoadIntegration("toms_storage").ifPresent { (it as Runnable).run() }
        // Register peripheral provider
        ForgeComputerCraftAPI.registerPeripheralProvider(IPeripheralProvider { world, pos, side ->
            val supplier = ComputerCraftProxy.lazyPeripheralProvider(world, pos, side)
                ?: return@IPeripheralProvider LazyOptional.empty()
            return@IPeripheralProvider LazyOptional.of { supplier.get() }
        })
    }

    fun registrySetup(event: NewRegistryEvent) {
        Platform.maybeLoadIntegration("integrateddynamics").ifPresent { (it as Runnable).run() }
        Platform.maybeLoadIntegration("naturescompass").ifPresent { (it as Runnable).run() }
    }
}