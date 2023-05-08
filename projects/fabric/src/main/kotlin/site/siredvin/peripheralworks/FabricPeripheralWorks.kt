package site.siredvin.peripheralworks
import com.tom.storagemod.block.InventoryConnectorBlock
import dan200.computercraft.api.ComputerCraftAPI
import dan200.computercraft.api.peripheral.PeripheralLookup
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraftforge.fml.config.ModConfig
import site.siredvin.peripheralium.FabricPeripheralium
import site.siredvin.peripheralworks.common.RegistrationQueue
import site.siredvin.peripheralworks.common.configuration.ConfigHolder
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.computercraft.FluidStorageProvider
import site.siredvin.peripheralworks.util.Platform


@Suppress("UNUSED")
object FabricPeripheralWorks: ModInitializer {

    override fun onInitialize() {
        // Register configuration
        FabricPeripheralium.sayHi()
        PeripheralWorksCore.configure()
        ComputerCraftProxy.addProvider(FluidStorageProvider)
        // Load all integrations
        Platform.maybeLoadIntegration("team_reborn_energy").ifPresent { (it as Runnable).run() }
        Platform.maybeLoadIntegration("naturescompass").ifPresent { (it as Runnable).run() }
        Platform.maybeLoadIntegration("toms_storage").ifPresent { (it as Runnable).run() }
        // Pretty important to setup configuration after integration loading!
        ForgeConfigRegistry.INSTANCE.register(PeripheralWorksCore.MOD_ID, ModConfig.Type.COMMON, ConfigHolder.COMMON_SPEC)
        // Register block lookup
        PeripheralLookup.get().registerFallback(ComputerCraftProxy::peripheralProvider)
        val event: Event<RegistryEntryAddedCallback<Registry<*>>> = RegistryEntryAddedCallback.event(BuiltInRegistries.REGISTRY) as Event<RegistryEntryAddedCallback<Registry<*>>>

        event.register(RegistrationQueue::onNewRegistry)
    }
}