package site.siredvin.peripheralworks.forge

import dan200.computercraft.api.network.wired.WiredElementCapability
import dan200.computercraft.api.peripheral.PeripheralCapability
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.InteractionResult
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract
import site.siredvin.broccolium.modules.base.block.FacingBlockEntityBlock
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.api.IPlatformItemStorageHolder
import site.siredvin.peripheralworks.common.block.PeripheralProxy
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
import site.siredvin.peripheralworks.common.blockentity.PeripheralProxyBlockEntity
import site.siredvin.peripheralworks.common.commands.DebugCommands
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.xplat.PeripheralWorksCommonHooks
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralProvider

@EventBusSubscriber(modid = PeripheralWorksCore.MOD_ID)
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

    fun registerCapabilities(event: RegisterCapabilitiesEvent) {
        BuiltInRegistries.BLOCK_ENTITY_TYPE.forEach { type ->
            @Suppress("UNCHECKED_CAST")
            event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                type as BlockEntityType<BlockEntity>,
            ) { blockEntity, _ ->
                (blockEntity as? IPlatformItemStorageHolder)?.getPlatformItemStorage() as? ForgeCustomSlottedStorage
            }
        }

        event.registerBlock(
            PeripheralCapability.get(),
            { level, pos, _, blockEntity, side ->
                (blockEntity as? IPeripheralProvider<*>)?.getPeripheral(side)
                    ?: ComputerCraftProxy.lazyPeripheralProvider(level, pos, side)?.get()
            },
            *BuiltInRegistries.BLOCK.toList().toTypedArray(),
        )

        event.registerBlockEntity(
            WiredElementCapability.get(),
            site.siredvin.peripheralworks.common.setup.BlockEntityTypes.PERIPHERAL_PROXY.get(),
        ) { blockEntity: PeripheralProxyBlockEntity, side ->
            if (side == blockEntity.blockState.getValue(PeripheralProxy.ORIENTATION).opposite) blockEntity.element else null
        }
        event.registerBlockEntity(
            WiredElementCapability.get(),
            site.siredvin.peripheralworks.common.setup.BlockEntityTypes.NETWORK_MANAGER.get(),
        ) { blockEntity: NetworkManagerBlockEntity, side ->
            if (side == blockEntity.blockState.getValue(FacingBlockEntityBlock.FACING)) null else blockEntity.element
        }
    }
}
