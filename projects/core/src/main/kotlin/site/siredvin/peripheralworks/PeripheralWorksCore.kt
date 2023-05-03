package site.siredvin.peripheralworks
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import site.siredvin.peripheralium.api.storage.ExtractorProxy
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.computercraft.StorageProvider
import site.siredvin.peripheralworks.computercraft.plugins.specific.SpecificPluginProvider
import site.siredvin.peripheralworks.utils.MinecartUtils


@Suppress("UNUSED")
object PeripheralWorksCore {
    const val MOD_ID = "peripheralworks"

    var LOGGER: Logger = LogManager.getLogger(MOD_ID)

    fun configure() {
        ExtractorProxy.addStorageExtractor(MinecartUtils::minecartExtractor)
        ComputerCraftProxy.addProvider(StorageProvider)
        ComputerCraftProxy.addProvider(SpecificPluginProvider)
    }
}