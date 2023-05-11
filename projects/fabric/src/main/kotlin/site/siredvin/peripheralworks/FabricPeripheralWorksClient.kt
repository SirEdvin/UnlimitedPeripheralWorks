package site.siredvin.peripheralworks

import net.fabricmc.api.ClientModInitializer

object FabricPeripheralWorksClient: ClientModInitializer {
    override fun onInitializeClient() {
        PeripheralWorksClientCore.onInit()
    }
}