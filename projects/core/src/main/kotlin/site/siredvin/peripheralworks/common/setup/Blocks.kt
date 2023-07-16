package site.siredvin.peripheralworks.common.setup

import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import site.siredvin.peripheralium.common.blocks.GenericBlockEntityBlock
import site.siredvin.peripheralium.common.items.PeripheralBlockItem
import site.siredvin.peripheralium.util.BlockUtil
import site.siredvin.peripheralworks.common.block.*
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.common.item.FlexibleRealityAnchorItem
import site.siredvin.peripheralworks.common.item.FlexibleStatueItem
import site.siredvin.peripheralworks.utils.TooltipCollection
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform

object Blocks {
    val PERIPHERAL_CASING = PeripheralWorksPlatform.registerBlock(
        "peripheral_casing",
        { Block(BlockUtil.defaultProperties()) },
    )
    val UNIVERSAL_SCANNER = PeripheralWorksPlatform.registerBlock(
        "universal_scanner",
        { GenericBlockEntityBlock({ BlockEntityTypes.UNIVERSAL_SCANNER.get() }, true) },
        {
            PeripheralBlockItem(
                it,
                Item.Properties(),
                PeripheralWorksConfig::enableUniversalScanner,
                alwaysShow = false,
                TooltipCollection::isDisabled,
                TooltipCollection::universalScanningRadius,
            )
        },
    )
    val ULTIMATE_SENSOR = PeripheralWorksPlatform.registerBlock(
        "ultimate_sensor",
        { GenericBlockEntityBlock({ BlockEntityTypes.ULTIMATE_SENSOR.get() }, true) },
        {
            PeripheralBlockItem(
                it,
                Item.Properties(),
                PeripheralWorksConfig::enableUltimateSensor,
                alwaysShow = true,
                TooltipCollection::isDisabled,
            )
        },
    )

    val ITEM_PEDESTAL = PeripheralWorksPlatform.registerBlock(
        "item_pedestal",
        ::ItemPedestal,
    ) {
        PeripheralBlockItem(
            it,
            Item.Properties(),
            PeripheralWorksConfig::enableItemPedestal,
            alwaysShow = true,
            TooltipCollection::isDisabled,
        )
    }

    val MAP_PEDESTAL = PeripheralWorksPlatform.registerBlock(
        "map_pedestal",
        ::MapPedestal,
    ) {
        PeripheralBlockItem(
            it,
            Item.Properties(),
            PeripheralWorksConfig::enableMapPedestal,
            alwaysShow = true,
            TooltipCollection::isDisabled,
        )
    }

    val DISPLAY_PEDESTAL = PeripheralWorksPlatform.registerBlock(
        "display_pedestal",
        ::DisplayPedestal,
    ) {
        PeripheralBlockItem(
            it,
            Item.Properties(),
            PeripheralWorksConfig::enableDisplayPedestal,
            alwaysShow = true,
            TooltipCollection::isDisabled,
        )
    }

    val REMOTE_OBSERVER = PeripheralWorksPlatform.registerBlock(
        "remote_observer",
        { GenericBlockEntityBlock({ BlockEntityTypes.REMOTE_OBSERVER.get() }, true, belongToTickingEntity = true) },
        {
            PeripheralBlockItem(
                it,
                Item.Properties(),
                PeripheralWorksConfig::enableRemoteObserver,
                alwaysShow = false,
                TooltipCollection::isDisabled,
                TooltipCollection::remoteObserverTooptips,
            )
        },
    )

    val PERIPHERAL_PROXY = PeripheralWorksPlatform.registerBlock(
        "peripheral_proxy",
        ::PeripheralProxy,
    ) {
        PeripheralBlockItem(
            it,
            Item.Properties(),
            PeripheralWorksConfig::enablePeripheralProxy,
            alwaysShow = true,
            TooltipCollection::isDisabled,
            TooltipCollection::peripheralProxyTooptips,
        )
    }
    val FLEXIBLE_REALITY_ANCHOR = PeripheralWorksPlatform.registerBlock(
        "flexible_reality_anchor",
        ::FlexibleRealityAnchor,
    ) {
        FlexibleRealityAnchorItem(it)
    }
    val REALITY_FORGER = PeripheralWorksPlatform.registerBlock(
        "reality_forger",
        { GenericBlockEntityBlock({ BlockEntityTypes.REALITY_FORGER.get() }, isRotatable = true, belongToTickingEntity = false) },
    ) {
        PeripheralBlockItem(
            it,
            Item.Properties(),
            PeripheralWorksConfig::enableRealityForger,
            alwaysShow = true,
            TooltipCollection::isDisabled,
            TooltipCollection::realityForgerTooptips,
        )
    }
    val RECIPE_REGISTRY = PeripheralWorksPlatform.registerBlock(
        "recipe_registry",
        { GenericBlockEntityBlock({ BlockEntityTypes.RECIPE_REGISTRY.get() }, isRotatable = true, belongToTickingEntity = false) },
    ) {
        PeripheralBlockItem(
            it,
            Item.Properties(),
            PeripheralWorksConfig::enableRecipeRegistry,
            alwaysShow = true,
            TooltipCollection::isDisabled,
        )
    }

    val INFORMATIVE_REGISTRY = PeripheralWorksPlatform.registerBlock(
        "informative_registry",
        { GenericBlockEntityBlock({ BlockEntityTypes.INFORMATIVE_REGISTRY.get() }, isRotatable = true, belongToTickingEntity = false) },
    ) {
        PeripheralBlockItem(
            it,
            Item.Properties(),
            PeripheralWorksConfig::enableInformativeRegistry,
            alwaysShow = true,
            TooltipCollection::isDisabled,
        )
    }

    val FLEXIBLE_STATUE = PeripheralWorksPlatform.registerBlock(
        "flexible_statue",
        ::FlexibleStatue,
    ) {
        FlexibleStatueItem(it)
    }

    val STATUE_WORKBENCH = PeripheralWorksPlatform.registerBlock(
        "statue_workbench",
        { StatueWorkbench() },
    ) {
        PeripheralBlockItem(
            it,
            Item.Properties(),
            PeripheralWorksConfig::enableStatueWorkbench,
            alwaysShow = true,
            TooltipCollection::isDisabled,
        )
    }

    fun doSomething() {}
}
