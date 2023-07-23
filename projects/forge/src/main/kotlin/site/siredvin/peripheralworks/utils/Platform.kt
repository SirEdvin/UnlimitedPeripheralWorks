package site.siredvin.peripheralworks.utils

import net.minecraftforge.fml.ModList
import site.siredvin.peripheralworks.ForgePeripheralWorks
import site.siredvin.peripheralworks.PeripheralWorksCore
import java.util.*

object Platform {
    fun maybeLoadIntegration(modid: String, path: String = "Integration"): Optional<Any> {
        val modPresent = ModList.get().isLoaded(modid)
        if (modPresent) {
            PeripheralWorksCore.LOGGER.info("Loading integration for $modid")
            return maybeLoadIntegration("$modid.$path")
        } else {
            PeripheralWorksCore.LOGGER.info("Mod $modid is not present, skip loading integration")
        }
        return Optional.empty()
    }

    private fun maybeLoadIntegration(path: String): Optional<Any> {
        return try {
            val clazz =
                Class.forName(ForgePeripheralWorks::class.java.getPackage().name + ".integrations." + path)
            Optional.of(clazz.getDeclaredConstructor().newInstance())
        } catch (ignored: InstantiationException) {
            Optional.empty()
        } catch (ignored: IllegalAccessException) {
            Optional.empty()
        } catch (ignored: ClassNotFoundException) {
            Optional.empty()
        } catch (e: Exception) {
            e.printStackTrace()
            Optional.empty()
        }
    }
}
