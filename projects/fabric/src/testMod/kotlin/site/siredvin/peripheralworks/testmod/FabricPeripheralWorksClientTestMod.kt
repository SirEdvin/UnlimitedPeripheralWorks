package site.siredvin.peripheralworks.testmod

import net.fabricmc.api.ClientModInitializer
import site.siredvin.testiarium.fixture.client.FabricClientTestHooks

object FabricPeripheralWorksClientTestMod : ClientModInitializer {
    override fun onInitializeClient() {
        FabricClientTestHooks.onInitializeClient()
    }
}
