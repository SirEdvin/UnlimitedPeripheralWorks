package site.siredvin.peripheralworks.common.setup

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntityType
import site.siredvin.peripheralium.xplat.PeripheraliumPlatform
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.blockentity.*
import site.siredvin.peripheralworks.utils.modId
import site.siredvin.peripheralworks.xplat.ModPlatform
import java.util.function.Supplier

object BlockEntityTypes {
    val UNIVERSAL_SCANNER: Supplier<BlockEntityType<UniversalScannerBlockEntity>> = ModPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "universal_scanner"),
    ) {
        PeripheraliumPlatform.createBlockEntityType(
            ::UniversalScannerBlockEntity,
            Blocks.UNIVERSAL_SCANNER.get(),
        )
    }

    val ULTIMATE_SENSOR: Supplier<BlockEntityType<UltimateSensorBlockEntity>> = ModPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "ultimate_sensor"),
    ) {
        PeripheraliumPlatform.createBlockEntityType(
            ::UltimateSensorBlockEntity,
            Blocks.ULTIMATE_SENSOR.get(),
        )
    }

    val ITEM_PEDESTAL: Supplier<BlockEntityType<ItemPedestalBlockEntity>> = ModPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "item_pedestal"),
    ) {
        PeripheraliumPlatform.createBlockEntityType(
            ::ItemPedestalBlockEntity,
            Blocks.ITEM_PEDESTAL.get(),
        )
    }

    val MAP_PEDESTAL: Supplier<BlockEntityType<MapPedestalBlockEntity>> = ModPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "map_pedestal"),
    ) {
        PeripheraliumPlatform.createBlockEntityType(
            ::MapPedestalBlockEntity,
            Blocks.MAP_PEDESTAL.get(),
        )
    }

    val DISPLAY_PEDESTAL: Supplier<BlockEntityType<DisplayPedestalBlockEntity>> = ModPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "display_pedestal"),
    ) {
        PeripheraliumPlatform.createBlockEntityType(
            ::DisplayPedestalBlockEntity,
            Blocks.DISPLAY_PEDESTAL.get(),
        )
    }

    val REMOTE_OBSERVER: Supplier<BlockEntityType<RemoteObserverBlockEntity>> = ModPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "remote_observer"),
    ) {
        PeripheraliumPlatform.createBlockEntityType(
            ::RemoteObserverBlockEntity,
            Blocks.REMOTE_OBSERVER.get(),
        )
    }

    val PERIPHERAL_PROXY: Supplier<BlockEntityType<PeripheralProxyBlockEntity>> = ModPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "peripheral_proxy"),
    ) {
        PeripheraliumPlatform.createBlockEntityType(
            ::PeripheralProxyBlockEntity,
            Blocks.PERIPHERAL_PROXY.get(),
        )
    }

    val FLEXIBLE_REALITY_ANCHOR = ModPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "flexible_reality_anchor"),
    ) {
        PeripheraliumPlatform.createBlockEntityType(
            ::FlexibleRealityAnchorBlockEntity,
            Blocks.FLEXIBLE_REALITY_ANCHOR.get(),
        )
    }

    val REALITY_FORGER: Supplier<BlockEntityType<RealityForgerBlockEntity>> = ModPlatform.registerBlockEntity(
        modId("reality_forger"),
    ) {
        PeripheraliumPlatform.createBlockEntityType(
            ::RealityForgerBlockEntity,
            Blocks.REALITY_FORGER.get(),
        )
    }

    val RECIPE_REGISTRY: Supplier<BlockEntityType<RecipeRegistryBlockEntity>> = ModPlatform.registerBlockEntity(
        modId("recipe_registry"),
    ) {
        PeripheraliumPlatform.createBlockEntityType(
            ::RecipeRegistryBlockEntity,
            Blocks.RECIPE_REGISTRY.get(),
        )
    }

    val INFORMATIVE_REGISTRY: Supplier<BlockEntityType<InformativeRegistryBlockEntity>> = ModPlatform.registerBlockEntity(
        modId("informative_registry"),
    ) {
        PeripheraliumPlatform.createBlockEntityType(
            ::InformativeRegistryBlockEntity,
            Blocks.INFORMATIVE_REGISTRY.get(),
        )
    }

    val FLEXIBLE_STATUE = ModPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "flexible_statue"),
    ) {
        PeripheraliumPlatform.createBlockEntityType(
            ::FlexibleStatueBlockEntity,
            Blocks.FLEXIBLE_STATUE.get(),
        )
    }

    val STATUE_WORKBENCH: Supplier<BlockEntityType<StatueWorkbenchBlockEntity>> = ModPlatform.registerBlockEntity(
        modId("statue_workbench"),
    ) {
        PeripheraliumPlatform.createBlockEntityType(
            ::StatueWorkbenchBlockEntity,
            Blocks.STATUE_WORKBENCH.get(),
        )
    }

    val ENTITY_LINK: Supplier<BlockEntityType<EntityLinkBlockEntity>> = ModPlatform.registerBlockEntity(
        modId("entity_link"),
    ) {
        PeripheraliumPlatform.createBlockEntityType(
            ::EntityLinkBlockEntity,
            Blocks.ENTITY_LINK.get(),
        )
    }

    fun doSomething() {}
}
