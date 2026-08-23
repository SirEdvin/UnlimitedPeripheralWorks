package site.siredvin.peripheralworks.testmod

import net.fabricmc.api.EnvType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import site.siredvin.testiarium.FabricTestiarium
import site.siredvin.testiarium.Testiarium
import site.siredvin.testiarium.cct.CctComputers
import site.siredvin.testiarium.cct.CctFixtureCommands

object FabricPeripheralWorksTestMod : ModInitializer {
    override fun onInitialize() {
        CctComputers.initialize()
        ServerLifecycleEvents.SERVER_STARTING.register {
            CctComputers.reset()
            CctFixtureCommands.importFiles(it)
        }
        Testiarium.register(PeripheralWorksGameTests::class.java)
        if (FabricLoader.getInstance().isModLoaded("ae2")) {
            Testiarium.register(Class.forName("site.siredvin.peripheralworks.testmod.AE2GameTests"))
            Testiarium.register(Class.forName("site.siredvin.peripheralworks.testmod.AE2ConfigurableObjectsGameTests"))
            Testiarium.register(Class.forName("site.siredvin.peripheralworks.testmod.AE2WirelessTerminalGameTests"))
        }
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
            Testiarium.register(Class.forName("site.siredvin.peripheralworks.testmod.NetworkManagerClientGameTests"))
        }
        FabricTestiarium.registerTests()
    }
}
