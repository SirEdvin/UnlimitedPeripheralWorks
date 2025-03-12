package site.siredvin.peripheralworks.data

import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import site.siredvin.broccolium.modules.data.ForgeGeneratorSink
import site.siredvin.peripheralworks.PeripheralWorksCore

@Mod.EventBusSubscriber(modid = PeripheralWorksCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
object ForgeDataGenerators {
    @SubscribeEvent
    fun genData(event: GatherDataEvent) {
        val generator = event.generator
        ModDataProviders.add(
            ForgeGeneratorSink(
                generator.getVanillaPack(true),
                event.existingFileHelper,
                event.lookupProvider,
            ),
        )
    }
}
