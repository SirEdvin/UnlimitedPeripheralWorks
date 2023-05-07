package site.siredvin.peripheralworks.data

import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import site.siredvin.peripheralworks.PeripheralWorksCore

@Mod.EventBusSubscriber(modid = PeripheralWorksCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
object ForgeDataGenerators {
    @SubscribeEvent
    fun genData(event: GatherDataEvent) {
        val generator = event.generator
        generator.addProvider(event.includeServer(), ModTurtleUpgradeDataProvider(generator.packOutput))
        generator.addProvider(event.includeServer(), ModPocketUpgradeDataProvider(generator.packOutput))
    }
}