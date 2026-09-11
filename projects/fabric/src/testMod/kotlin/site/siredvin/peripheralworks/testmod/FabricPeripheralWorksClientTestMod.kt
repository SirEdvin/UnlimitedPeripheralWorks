package site.siredvin.peripheralworks.testmod

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import site.siredvin.testiarium.fixture.client.ClientTestHooks

object FabricPeripheralWorksClientTestMod : ClientModInitializer {
    override fun onInitializeClient() {
        if (System.getProperty("testiarium.client") == null) return
        ClientTickEvents.END_CLIENT_TICK.register { client -> client.screen?.let(ClientTestHooks::onOpenScreen) }
        ServerTickEvents.START_SERVER_TICK.register(ClientTestHooks::onServerTick)
    }
}
