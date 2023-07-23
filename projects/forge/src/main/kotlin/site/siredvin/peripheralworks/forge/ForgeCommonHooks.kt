package site.siredvin.peripheralworks.forge

import net.minecraft.world.InteractionResult
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.commands.DebugCommands
import site.siredvin.peripheralworks.xplat.PeripheralWorksCommonHooks

@Mod.EventBusSubscriber(modid = PeripheralWorksCore.MOD_ID)
object ForgeCommonHooks {
    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        DebugCommands.register(event.dispatcher)
    }

    @SubscribeEvent
    fun entityClick(event: EntityInteract) {
        val shouldCancel = PeripheralWorksCommonHooks.onEntityRightClick(event.entity, event.target)
        if (shouldCancel) {
            event.isCanceled = true
            event.cancellationResult = InteractionResult.SUCCESS
        }
    }
}
