package site.siredvin.peripheralworks

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent

@Mod.EventBusSubscriber(modid = PeripheralWorksCore.MOD_ID, value = [Dist.CLIENT], bus = Mod.EventBusSubscriber.Bus.MOD)
object ForgePeripheralWorksClient {

    private val CLIENT_HOOKS: MutableList<Runnable> = mutableListOf()

    fun registerHook(it: Runnable) {
        CLIENT_HOOKS.add(it)
    }

    @SubscribeEvent
    fun onClientSetup(event: FMLClientSetupEvent) {
        CLIENT_HOOKS.forEach { it.run() }
    }
}