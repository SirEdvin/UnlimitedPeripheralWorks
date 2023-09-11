package site.siredvin.peripheralworks
import dan200.computercraft.api.peripheral.PeripheralLookup
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.GameType
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult
import net.minecraftforge.fml.config.ModConfig
import site.siredvin.peripheralium.FabricPeripheralium
import site.siredvin.peripheralium.api.peripheral.IPeripheralProvider
import site.siredvin.peripheralium.loader.FabricIntegrationLoader
import site.siredvin.peripheralworks.common.commands.DebugCommands
import site.siredvin.peripheralworks.common.configuration.ConfigHolder
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.fabric.FabricModBlocksReference
import site.siredvin.peripheralworks.fabric.FabricModPlatform
import site.siredvin.peripheralworks.fabric.FabricModRecipeIngredients
import site.siredvin.peripheralworks.subsystem.recipe.FabricRecipeTransformers
import site.siredvin.peripheralworks.xplat.PeripheralWorksCommonHooks

@Suppress("UNUSED")
object FabricPeripheralWorks : ModInitializer {

    val loader = FabricIntegrationLoader(
        FabricPeripheralWorks::class.java.getPackage().name,
        PeripheralWorksCore.LOGGER,
    )

    override fun onInitialize() {
        // Register configuration
        FabricPeripheralium.sayHi()
        PeripheralWorksCore.configure(FabricModPlatform, FabricModRecipeIngredients, FabricModBlocksReference)
        // Register items and blocks
        PeripheralWorksCommonHooks.onRegister()
        // Load all integrations
        loader.maybeLoadIntegration("automobility").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("ae2").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("team_reborn_energy").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("naturescompass").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("toms_storage").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("additionallanterns").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("alloy_forgery").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("universal_shops").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("powah").ifPresent { (it as Runnable).run() }
        loader.maybeLoadIntegration("modern_industrialization").ifPresent { (it as Runnable).run() }
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

        UseEntityCallback.EVENT.register(
            UseEntityCallback { player: Player, _: Level, hand: InteractionHand, entity: Entity, _: EntityHitResult? ->
                if (player is ServerPlayer) {
                    if (player.gameMode.gameModeForPlayer == GameType.SPECTATOR) {
                        return@UseEntityCallback InteractionResult.PASS
                    }
                } else if (player is AbstractClientPlayer) {
                    if (player.isSpectator) {
                        return@UseEntityCallback InteractionResult.PASS
                    }
                }
                if (hand == InteractionHand.MAIN_HAND) {
                    val shouldCancel = PeripheralWorksCommonHooks.onEntityRightClick(player, entity)
                    if (shouldCancel) {
                        return@UseEntityCallback InteractionResult.SUCCESS
                    }
                }
                return@UseEntityCallback InteractionResult.PASS
            },
        )

        FabricRecipeTransformers.init()
    }
}
