package site.siredvin.peripheralworks.util

import net.fabricmc.loader.api.FabricLoader
import site.siredvin.peripheralworks.PeripheralWorks
import java.lang.InstantiationException
import java.lang.IllegalAccessException
import java.lang.ClassNotFoundException
import java.lang.Exception
import java.util.*

object Platform {
    fun maybeLoadIntegration(modid: String, path: String = "Integration"): Optional<Any> {
        val modPresent = FabricLoader.getInstance().allMods.stream().anyMatch { it.metadata.id == modid }
        if (modPresent) {
            PeripheralWorks.LOGGER.info("Loading integration for $modid")
            return maybeLoadIntegration("${modid}.$path")
        } else {
            PeripheralWorks.LOGGER.info("Mod $modid is not present, skip loading integration")
        }
        return Optional.empty()
    }

    private fun maybeLoadIntegration(path: String): Optional<Any> {
        return try {
            val clazz = Class.forName(PeripheralWorks::class.java.getPackage().name + ".integrations." + path)
            Optional.of(clazz.newInstance())
        } catch (ignored: InstantiationException) {
            PeripheralWorks.LOGGER.info("Exception when loading integration $ignored")
            Optional.empty()
        } catch (ignored: IllegalAccessException) {
            PeripheralWorks.LOGGER.info("Exception when loading integration $ignored")
            Optional.empty()
        } catch (ignored: ClassNotFoundException) {
            PeripheralWorks.LOGGER.info("Exception when loading integration $ignored")
            Optional.empty()
        } catch (e: Exception) {
            e.printStackTrace()
            Optional.empty()
        }
    }
}