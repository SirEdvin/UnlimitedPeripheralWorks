package site.siredvin.peripheralworks
import dan200.computercraft.api.ComputerCraftAPI
import net.fabricmc.api.ModInitializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.util.Platform


@Suppress("UNUSED")
object PeripheralWorks: ModInitializer {
    const val MOD_ID = "peripheralworks"

    var LOGGER: Logger = LogManager.getLogger(MOD_ID)

    override fun onInitialize() {
        ComputerCraftAPI.registerPeripheralProvider(ComputerCraftProxy::peripheralProvider)

        Platform.maybeLoadIntegration("team_reborn_energy").ifPresent { (it as Runnable).run() }
    }
}