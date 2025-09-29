package site.siredvin.peripheralworks.forge

import dan200.computercraft.api.ComputerCraftAPI
import dan200.computercraft.shared.Capabilities.CAPABILITY_WIRED_ELEMENT
import dan200.computercraft.shared.util.SidedCapabilityProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionResult
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.block.PeripheralProxy
import site.siredvin.peripheralworks.common.blockentity.PeripheralProxyBlockEntity
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

    @SubscribeEvent
    fun onCapability(event: AttachCapabilitiesEvent<BlockEntity>) {
        val be = event.`object`
        if (be is PeripheralProxyBlockEntity) {
            SidedCapabilityProvider.attach(
                event,
                ResourceLocation(ComputerCraftAPI.MOD_ID, "wired_node"),
                CAPABILITY_WIRED_ELEMENT,
                {
                    if (it == be.blockState.getValue(PeripheralProxy.ORIENTATION).opposite) {
                        return@attach be.element
                    }
                    return@attach null
                },
            )
        }
    }
}
