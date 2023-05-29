package site.siredvin.peripheralworks.util

import net.fabricmc.loader.api.FabricLoader
import site.siredvin.peripheralworks.FabricPeripheralWorks
import site.siredvin.peripheralworks.PeripheralWorksCore
import java.lang.ClassNotFoundException
import java.lang.Exception
import java.lang.IllegalAccessException
import java.lang.InstantiationException
import java.util.*

object Platform {
    fun maybeLoadIntegration(modid: String, path: String = "Integration"): Optional<Any> {
        val modPresent = FabricLoader.getInstance().allMods.stream().anyMatch { it.metadata.id == modid }
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
            val clazz = Class.forName(FabricPeripheralWorks::class.java.getPackage().name + ".integrations." + path)
            Optional.of(clazz.newInstance())
        } catch (ignored: InstantiationException) {
            PeripheralWorksCore.LOGGER.info("Exception when loading integration $ignored")
            Optional.empty()
        } catch (ignored: IllegalAccessException) {
            PeripheralWorksCore.LOGGER.info("Exception when loading integration $ignored")
            Optional.empty()
        } catch (ignored: ClassNotFoundException) {
            PeripheralWorksCore.LOGGER.info("Exception when loading integration $ignored")
            Optional.empty()
        } catch (e: Exception) {
            e.printStackTrace()
            Optional.empty()
        }
    }
}
