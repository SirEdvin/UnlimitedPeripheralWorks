package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.shared.turtle.blocks.TurtleBlock
import dan200.computercraft.shared.turtle.blocks.TurtleBlockEntity
import net.minecraft.world.item.ItemStack
import site.siredvin.broccolium.modules.storage.base.api.SlottedAgnosticStorage
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStorageLookup
import site.siredvin.broccolium.modules.storage.item.AgnosticItemStorageLookup
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.plugins.ComputerPlugin
import site.siredvin.tweakium.modules.peripheral.OwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.owner.RawBlockEntityPeripheralOwner
import site.siredvin.tweakium.modules.plugins.EnergyPlugin
import site.siredvin.tweakium.modules.plugins.FullEnergyPlugin
import site.siredvin.tweakium.modules.plugins.InventoryPlugin

class TweakedTurtlePeripheral(turtle: TurtleBlockEntity) :
    OwnedPeripheral<RawBlockEntityPeripheralOwner<TurtleBlockEntity>>(
        "turtle",
        RawBlockEntityPeripheralOwner(turtle, TurtleBlock.FACING),
    ) {
    override val isEnabled: Boolean
        get() = true

    init {
        val level = turtle.level!!
        val itemStorage = AgnosticItemStorageLookup.extractFromBlock(level, turtle.blockPos, turtle, null) as SlottedAgnosticStorage<ItemStack, Int>
        val energyStorage = AgnosticEnergyStorageLookup.extractFromBlock(level, turtle.blockPos, turtle, null)!!
        addPlugin(ComputerPlugin("turtle", turtle))
        addPlugin(InventoryPlugin(level, itemStorage, PeripheralWorksConfig.itemStorageTransferLimit))
        if (PeripheralWorksConfig.energyAlwaysTransferable) {
            addPlugin(FullEnergyPlugin(level, energyStorage, PeripheralWorksConfig.energyStorageTransferLimit))
        } else {
            addPlugin(EnergyPlugin(energyStorage))
        }
    }
}
