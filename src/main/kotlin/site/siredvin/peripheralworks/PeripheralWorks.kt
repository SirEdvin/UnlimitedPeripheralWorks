package site.siredvin.peripheralworks
import dan200.computercraft.api.ComputerCraftAPI
import net.fabricmc.api.ModInitializer
import net.minecraftforge.api.ModLoadingContext
import net.minecraftforge.fml.config.ModConfig
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import site.siredvin.peripheralworks.common.configuration.ConfigHolder
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.util.Platform


@Suppress("UNUSED")
object PeripheralWorks: ModInitializer {
    const val MOD_ID = "peripheralworks"

    var LOGGER: Logger = LogManager.getLogger(MOD_ID)

    override fun onInitialize() {
        ComputerCraftAPI.registerPeripheralProvider(ComputerCraftProxy::peripheralProvider)
        // Load all integrations
        Platform.maybeLoadIntegration("team_reborn_energy").ifPresent { (it as Runnable).run() }
        // Register configuration
        // Pretty important to setup configuration after integration loading!
        ModLoadingContext.registerConfig(MOD_ID, ModConfig.Type.COMMON, ConfigHolder.COMMON_SPEC)
    }
}