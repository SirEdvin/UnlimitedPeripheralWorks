package site.siredvin.peripheralworks
import dan200.computercraft.api.peripheral.PeripheralLookup
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.fml.config.ModConfig
import site.siredvin.peripheralium.FabricPeripheralium
import site.siredvin.peripheralium.api.peripheral.IPeripheralProvider
import site.siredvin.peripheralworks.common.RegistrationQueue
import site.siredvin.peripheralworks.common.commands.DebugCommands
import site.siredvin.peripheralworks.common.configuration.ConfigHolder
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.computercraft.FluidStorageProvider
import site.siredvin.peripheralworks.fabric.FabricModRecipeIngredients
import site.siredvin.peripheralworks.fabric.FabricPeripheralWorksPlatform
import site.siredvin.peripheralworks.util.Platform
import site.siredvin.peripheralworks.xplat.PeripheralWorksCommonHooks

@Suppress("UNUSED")
object FabricPeripheralWorks : ModInitializer {

    override fun onInitialize() {
        // Register configuration
        FabricPeripheralium.sayHi()
        PeripheralWorksCore.configure(FabricPeripheralWorksPlatform, FabricModRecipeIngredients)
        PeripheralWorksCore.configureCreativeTab(
            FabricItemGroup.builder(
                ResourceLocation(PeripheralWorksCore.MOD_ID, "tab"),
            ),
        ).build()
        ComputerCraftProxy.addProvider(FluidStorageProvider)
        // Register items and blocks
        PeripheralWorksCommonHooks.onRegister()
        // Load all integrations
        Platform.maybeLoadIntegration("team_reborn_energy").ifPresent { (it as Runnable).run() }
        Platform.maybeLoadIntegration("naturescompass").ifPresent { (it as Runnable).run() }
        Platform.maybeLoadIntegration("toms_storage").ifPresent { (it as Runnable).run() }
        // Pretty important to setup configuration after integration loading!
        ForgeConfigRegistry.INSTANCE.register(PeripheralWorksCore.MOD_ID, ModConfig.Type.COMMON, ConfigHolder.COMMON_SPEC)
        // Register block lookup
        PeripheralLookup.get().registerFallback { world, pos, state, blockEntity, context ->
            if (blockEntity is IPeripheralProvider<*>) {
                return@registerFallback blockEntity.getPeripheral(context)
            }
            return@registerFallback ComputerCraftProxy.peripheralProvider(world, pos, state, blockEntity, context)
        }

        val event: Event<RegistryEntryAddedCallback<Registry<*>>> = RegistryEntryAddedCallback.event(BuiltInRegistries.REGISTRY) as Event<RegistryEntryAddedCallback<Registry<*>>>
        event.register(RegistrationQueue::onNewRegistry)

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            DebugCommands.register(dispatcher)
        }
    }
}
