package site.siredvin.peripheralworks.forge

import dan200.computercraft.api.ComputerCraftAPI
import dan200.computercraft.shared.Capabilities.CAPABILITY_WIRED_ELEMENT
import dan200.computercraft.shared.util.SidedCapabilityProvider
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionResult
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import site.siredvin.broccolium.modules.base.block.FacingBlockEntityBlock
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.api.IPlatformItemStorageHolder
import site.siredvin.peripheralworks.common.block.PeripheralProxy
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
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
        if (be is IPlatformItemStorageHolder) {
            event.addCapability(
                ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "item_handler"),
                object : ICapabilityProvider {
                    override fun <T> getCapability(
                        cap: Capability<T>,
                        side: Direction?,
                    ): LazyOptional<T> = ForgeCapabilities.ITEM_HANDLER.orEmpty(
                        cap,
                        LazyOptional.of { be.getPlatformItemStorage() as ForgeCustomSlottedStorage },
                    )
                },
            )
        }
        if (be is PeripheralProxyBlockEntity) {
            SidedCapabilityProvider.attach(
                event,
                ResourceLocation.fromNamespaceAndPath(ComputerCraftAPI.MOD_ID, "wired_node"),
                CAPABILITY_WIRED_ELEMENT,
                {
                    if (it == be.blockState.getValue(PeripheralProxy.ORIENTATION).opposite) {
                        return@attach be.element
                    }
                    return@attach null
                },
            )
        }
        if (be is NetworkManagerBlockEntity) {
            SidedCapabilityProvider.attach(
                event,
                ResourceLocation.fromNamespaceAndPath(ComputerCraftAPI.MOD_ID, "wired_node"),
                CAPABILITY_WIRED_ELEMENT,
                {
                    if (it == be.blockState.getValue(FacingBlockEntityBlock.FACING)) {
                        return@attach null
                    }
                    return@attach be.element
                },
            )
        }
    }
}
