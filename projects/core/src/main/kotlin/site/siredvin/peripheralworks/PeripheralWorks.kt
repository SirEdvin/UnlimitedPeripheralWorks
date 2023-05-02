package site.siredvin.peripheralworks
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import site.siredvin.peripheralium.api.storage.ExtractorProxy
import site.siredvin.peripheralworks.utils.MinecartUtils


@Suppress("UNUSED")
object PeripheralWorks {
    const val MOD_ID = "peripheralworks"

    var LOGGER: Logger = LogManager.getLogger(MOD_ID)

    fun configure() {
        ExtractorProxy.addStorageExtractor(MinecartUtils::minecartExtractor)
    }
}