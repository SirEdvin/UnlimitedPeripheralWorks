package site.siredvin.peripheralworks.testmod

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
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
        FabricTestiarium.registerTests()
    }
}
