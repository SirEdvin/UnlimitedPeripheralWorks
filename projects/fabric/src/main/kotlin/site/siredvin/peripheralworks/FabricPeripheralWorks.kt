package site.siredvin.peripheralworks
import dan200.computercraft.api.peripheral.PeripheralLookup
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraftforge.fml.config.ModConfig
import site.siredvin.peripheralium.FabricPeripheralium
import site.siredvin.peripheralium.api.peripheral.IPeripheralProvider
import site.siredvin.peripheralworks.common.commands.DebugCommands
import site.siredvin.peripheralworks.common.configuration.ConfigHolder
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.fabric.FabricModBlocksReference
import site.siredvin.peripheralworks.fabric.FabricModRecipeIngredients
import site.siredvin.peripheralworks.fabric.FabricPeripheralWorksPlatform
import site.siredvin.peripheralworks.util.Platform
import site.siredvin.peripheralworks.xplat.PeripheralWorksCommonHooks

@Suppress("UNUSED")
object FabricPeripheralWorks : ModInitializer {

    override fun onInitialize() {
        // Register configuration
        FabricPeripheralium.sayHi()
        PeripheralWorksCore.configure(FabricPeripheralWorksPlatform, FabricModRecipeIngredients, FabricModBlocksReference)
        // Register items and blocks
        PeripheralWorksCommonHooks.onRegister()
        // Load all integrations
        Platform.maybeLoadIntegration("ae2").ifPresent { (it as Runnable).run() }
        Platform.maybeLoadIntegration("team_reborn_energy").ifPresent { (it as Runnable).run() }
        Platform.maybeLoadIntegration("naturescompass").ifPresent { (it as Runnable).run() }
        Platform.maybeLoadIntegration("toms_storage").ifPresent { (it as Runnable).run() }
        Platform.maybeLoadIntegration("additionallanterns").ifPresent { (it as Runnable).run() }
        Platform.maybeLoadIntegration("alloy_forgery").ifPresent { (it as Runnable).run() }
        Platform.maybeLoadIntegration("universal_shops").ifPresent { (it as Runnable).run() }
        // Pretty important to setup configuration after integration loading!
        ForgeConfigRegistry.INSTANCE.register(PeripheralWorksCore.MOD_ID, ModConfig.Type.COMMON, ConfigHolder.COMMON_SPEC)
        // Register block lookup
        PeripheralLookup.get().registerFallback { world, pos, state, blockEntity, context ->
            if (blockEntity is IPeripheralProvider<*>) {
                return@registerFallback blockEntity.getPeripheral(context)
            }
            return@registerFallback ComputerCraftProxy.peripheralProvider(world, pos, state, blockEntity, context)
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            DebugCommands.register(dispatcher)
        }
    }
}
