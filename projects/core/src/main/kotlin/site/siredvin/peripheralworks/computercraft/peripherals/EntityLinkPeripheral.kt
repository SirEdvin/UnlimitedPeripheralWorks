package site.siredvin.peripheralworks.computercraft.peripherals

import site.siredvin.peripheralium.computercraft.peripheral.OwnedPeripheral
import site.siredvin.peripheralium.computercraft.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.peripheralworks.common.blockentity.EntityLinkBlockEntity

class EntityLinkPeripheral(private val blockEntity: EntityLinkBlockEntity) : OwnedPeripheral<BlockEntityPeripheralOwner<EntityLinkBlockEntity>>(TYPE, BlockEntityPeripheralOwner(blockEntity)) {
    companion object {
        const val TYPE = "entity_link"
    }

    override val isEnabled: Boolean
        // TODO: update this
        get() = true
}
