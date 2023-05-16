package site.siredvin.peripheralworks.common.setup

import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.Material
import site.siredvin.peripheralium.common.blocks.GenericBlockEntityBlock
import site.siredvin.peripheralium.common.items.PeripheralBlockItem
import site.siredvin.peripheralium.util.BlockUtil
import site.siredvin.peripheralworks.common.block.DisplayPedestal
import site.siredvin.peripheralworks.common.block.ItemPedestal
import site.siredvin.peripheralworks.common.block.MapPedestal
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.utils.TooltipCollection
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform

object Blocks {
    val PERIPHERAL_CASING = PeripheralWorksPlatform.registerBlock(
        "peripheral_casing", { Block(BlockUtil.defaultProperties()) },
    )
    val UNIVERSAL_SCANNER = PeripheralWorksPlatform.registerBlock(
        "universal_scanner", { GenericBlockEntityBlock({ BlockEntityTypes.UNIVERSAL_SCANNER.get() }, true) },
        {PeripheralBlockItem(
            it, Item.Properties(), PeripheralWorksConfig::enableUniversalScanner, alwaysShow = false,
            TooltipCollection::isDisabled, TooltipCollection::universalScanningFreeRadius, TooltipCollection::universalScanningMaxRadius
        ) }
    )
    val ULTIMATE_SENSOR = PeripheralWorksPlatform.registerBlock(
        "ultimate_sensor", { GenericBlockEntityBlock({ BlockEntityTypes.ULTIMATE_SENSOR.get() }, true) },
        {PeripheralBlockItem(
            it, Item.Properties(), PeripheralWorksConfig::enableUltimateSensor, alwaysShow = true,
            TooltipCollection::isDisabled
        ) }
    )

    val ITEM_PEDESTAL = PeripheralWorksPlatform.registerBlock(
        "item_pedestal", { ItemPedestal() },
        { PeripheralBlockItem(it, Item.Properties(), PeripheralWorksConfig::enableItemPedestal,  alwaysShow = true, TooltipCollection::isDisabled) }
    )

    val MAP_PEDESTAL = PeripheralWorksPlatform.registerBlock(
        "map_pedestal", { MapPedestal() },
        { PeripheralBlockItem(it, Item.Properties(), PeripheralWorksConfig::enableMapPedestal,  alwaysShow = true, TooltipCollection::isDisabled) }
    )

    val DISPLAY_PEDESTAL = PeripheralWorksPlatform.registerBlock(
        "display_pedestal", { DisplayPedestal() },
        { PeripheralBlockItem(it, Item.Properties(), PeripheralWorksConfig::enableDisplayPedestal,  alwaysShow = true, TooltipCollection::isDisabled) }
    )

    fun doSomething() {}
}