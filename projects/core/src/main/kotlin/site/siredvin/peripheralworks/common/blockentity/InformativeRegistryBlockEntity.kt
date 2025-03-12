package site.siredvin.peripheralworks.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.computercraft.peripherals.InformativeRegistryPeripheral
import site.siredvin.tweakium.modules.peripheral.blockentity.PeripheralBlockEntity

class InformativeRegistryBlockEntity(blockPos: BlockPos, blockState: BlockState) : PeripheralBlockEntity<InformativeRegistryPeripheral>(BlockEntityTypes.INFORMATIVE_REGISTRY.get(), blockPos, blockState) {
    override fun createPeripheral(side: Direction): InformativeRegistryPeripheral = InformativeRegistryPeripheral(this)
}
