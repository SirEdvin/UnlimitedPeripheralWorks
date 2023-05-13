package site.siredvin.peripheralworks

import net.minecraft.resources.ResourceLocation
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.ModelEvent.RegisterAdditional
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent

@Mod.EventBusSubscriber(modid = PeripheralWorksCore.MOD_ID, value = [Dist.CLIENT], bus = Mod.EventBusSubscriber.Bus.MOD)
object ForgePeripheralWorksClient {

    @SubscribeEvent
    fun onClientSetup(event: FMLClientSetupEvent) {
        PeripheralWorksClientCore.onInit()
    }

    @SubscribeEvent
    fun registerModels(event: RegisterAdditional) {
        PeripheralWorksClientCore.registerExtraModels { model: ResourceLocation ->
            event.register(model)
        }
    }
}