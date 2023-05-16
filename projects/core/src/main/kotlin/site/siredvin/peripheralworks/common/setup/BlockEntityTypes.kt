package site.siredvin.peripheralworks.common.setup

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntityType
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.blockentity.*
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform
import java.util.function.Supplier

object BlockEntityTypes {
    val UNIVERSAL_SCANNER: Supplier<BlockEntityType<UniversalScannerBlockEntity>> = PeripheralWorksPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "universal_scanner")
    ) {
        PeripheralWorksPlatform.createBlockEntityType(
            { pos, state -> UniversalScannerBlockEntity(pos, state) },
            Blocks.UNIVERSAL_SCANNER.get()
        )
    }

    val ULTIMATE_SENSOR: Supplier<BlockEntityType<UltimateSensorBlockEntity>> = PeripheralWorksPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "ultimate_sensor")
    ) {
        PeripheralWorksPlatform.createBlockEntityType(
            { pos, state -> UltimateSensorBlockEntity(pos, state) },
            Blocks.ULTIMATE_SENSOR.get()
        )
    }

    val ITEM_PEDESTAL: Supplier<BlockEntityType<ItemPedestalBlockEntity>> = PeripheralWorksPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "item_pedestal")
    ) {
        PeripheralWorksPlatform.createBlockEntityType(
            { pos, state -> ItemPedestalBlockEntity(pos, state) },
            Blocks.ITEM_PEDESTAL.get()
        )
    }

    val MAP_PEDESTAL: Supplier<BlockEntityType<MapPedestalBlockEntity>> = PeripheralWorksPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "map_pedestal")
    ) {
        PeripheralWorksPlatform.createBlockEntityType(
            { pos, state -> MapPedestalBlockEntity(pos, state) },
            Blocks.MAP_PEDESTAL.get()
        )
    }

    val DISPLAY_PEDESTAL: Supplier<BlockEntityType<DisplayPedestalBlockEntity>> = PeripheralWorksPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "display_pedestal")
    ) {
        PeripheralWorksPlatform.createBlockEntityType(
            { pos, state -> DisplayPedestalBlockEntity(pos, state) },
            Blocks.DISPLAY_PEDESTAL.get()
        )
    }

    fun doSomething() {}
}