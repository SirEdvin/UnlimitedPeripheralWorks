package site.siredvin.peripheralworks.common.setup

import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.Material
import site.siredvin.peripheralium.common.blocks.GenericBlockEntityBlock
import site.siredvin.peripheralium.common.items.PeripheralBlockItem
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.utils.TooltipCollection
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform

object Blocks {
    val PERIPHERAL_CASING = PeripheralWorksPlatform.registerBlock(
        "peripheral_casing", { Block(BlockBehaviour.Properties.of(Material.STONE).destroyTime(0.5f)) },
    )
    val UNIVERSAL_SCANNER = PeripheralWorksPlatform.registerBlock(
        "universal_scanner", { GenericBlockEntityBlock({ BlockEntityTypes.UNIVERSAL_SCANNER.get() }, true) },
        {PeripheralBlockItem(
            it, Item.Properties(), PeripheralWorksConfig::enableUniversalScanner,
            TooltipCollection::isDisabled, TooltipCollection::universalScanningFreeRadius, TooltipCollection::universalScanningMaxRadius
        ) }
    )
    val ULTIMATE_SENSOR = PeripheralWorksPlatform.registerBlock(
        "ultimate_sensor", { GenericBlockEntityBlock({ BlockEntityTypes.ULTIMATE_SENSOR.get() }, true) },
        {PeripheralBlockItem(
            it, Item.Properties(), PeripheralWorksConfig::enableUltimateSensor,
            TooltipCollection::isDisabled
        ) }
    )

    fun doSomething() {}
}