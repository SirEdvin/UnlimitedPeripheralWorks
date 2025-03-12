package site.siredvin.peripheralworks.computercraft.peripherals

import site.siredvin.peripheralworks.common.block.BasePedestal
import site.siredvin.peripheralworks.common.blockentity.ItemPedestalBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.plugins.PedestalInventoryPlugin
import site.siredvin.tweakium.modules.peripheral.OwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.owner.BlockEntityPeripheralOwner

class ItemPedestalPeripheral(blockEntity: ItemPedestalBlockEntity) :
    OwnedPeripheral<BlockEntityPeripheralOwner<ItemPedestalBlockEntity>>(
        TYPE,
        BlockEntityPeripheralOwner(blockEntity, facingProperty = BasePedestal.FACING),
    ) {
    companion object {
        const val TYPE = "item_pedestal"
    }

    init {
        addPlugin(PedestalInventoryPlugin(blockEntity))
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enableItemPedestal
}
