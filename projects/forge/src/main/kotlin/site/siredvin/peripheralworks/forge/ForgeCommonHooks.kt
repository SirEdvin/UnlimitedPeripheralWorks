package site.siredvin.peripheralworks.forge

import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.commands.DebugCommands

@Mod.EventBusSubscriber(modid = PeripheralWorksCore.MOD_ID)
object ForgeCommonHooks {
    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        DebugCommands.register(event.dispatcher)
    }
}