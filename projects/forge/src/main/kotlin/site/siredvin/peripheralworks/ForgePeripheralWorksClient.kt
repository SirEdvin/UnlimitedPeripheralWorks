package site.siredvin.peripheralworks

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent

@Mod.EventBusSubscriber(modid = PeripheralWorksCore.MOD_ID, value = [Dist.CLIENT], bus = Mod.EventBusSubscriber.Bus.MOD)
object ForgePeripheralWorksClient {

    @SubscribeEvent
    fun onClientSetup(event: FMLClientSetupEvent) {
        PeripheralWorksClientCore.onInit()
    }
}