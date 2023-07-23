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
import site.siredvin.peripheralworks.xplat.ModPlatform

object Blocks {
    val PERIPHERAL_CASING = ModPlatform.registerBlock(
        "peripheral_casing",
        { Block(BlockUtil.defaultProperties()) },
    )
    val UNIVERSAL_SCANNER = ModPlatform.registerBlock(
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
    val ULTIMATE_SENSOR = ModPlatform.registerBlock(
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

    val ITEM_PEDESTAL = ModPlatform.registerBlock(
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

    val MAP_PEDESTAL = ModPlatform.registerBlock(
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

    val DISPLAY_PEDESTAL = ModPlatform.registerBlock(
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

    val REMOTE_OBSERVER = ModPlatform.registerBlock(
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

    val PERIPHERAL_PROXY = ModPlatform.registerBlock(
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
    val FLEXIBLE_REALITY_ANCHOR = ModPlatform.registerBlock(
        "flexible_reality_anchor",
        ::FlexibleRealityAnchor,
    ) {
        FlexibleRealityAnchorItem(it)
    }
    val REALITY_FORGER = ModPlatform.registerBlock(
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
    val RECIPE_REGISTRY = ModPlatform.registerBlock(
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

    val INFORMATIVE_REGISTRY = ModPlatform.registerBlock(
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

    val FLEXIBLE_STATUE = ModPlatform.registerBlock(
        "flexible_statue",
        ::FlexibleStatue,
    ) {
        FlexibleStatueItem(it)
    }

    val STATUE_WORKBENCH = ModPlatform.registerBlock(
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
