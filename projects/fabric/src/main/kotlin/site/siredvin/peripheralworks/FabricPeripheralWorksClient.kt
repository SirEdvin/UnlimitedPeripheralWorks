package site.siredvin.peripheralworks

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import java.util.function.Consumer

object FabricPeripheralWorksClient: ClientModInitializer {
    override fun onInitializeClient() {
        PeripheralWorksClientCore.onInit()
        ModelLoadingRegistry.INSTANCE.registerModelProvider { _: ResourceManager, out: Consumer<ResourceLocation> ->
            PeripheralWorksClientCore.registerExtraModels(out)
        }
    }
}