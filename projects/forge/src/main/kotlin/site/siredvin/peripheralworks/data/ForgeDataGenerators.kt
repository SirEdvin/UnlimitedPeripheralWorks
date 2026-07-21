package site.siredvin.peripheralworks.data

import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.data.event.GatherDataEvent
import site.siredvin.broccolium.modules.data.ForgeGeneratorSink
import site.siredvin.peripheralworks.PeripheralWorksCore

@EventBusSubscriber(modid = PeripheralWorksCore.MOD_ID)
object ForgeDataGenerators {
    @SubscribeEvent
    fun genData(event: GatherDataEvent) {
        ModDataProviders.add(
            ForgeGeneratorSink(event.generator, event),
        )
    }
}
