package site.siredvin.peripheralworks.common.setup

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntityType
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.blockentity.*
import site.siredvin.peripheralworks.utils.modId
import site.siredvin.peripheralworks.xplat.ModPlatform
import java.util.function.Supplier

@Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
object BlockEntityTypes {
    val UNIVERSAL_SCANNER: Supplier<BlockEntityType<UniversalScannerBlockEntity>> = ModPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "universal_scanner"),
    ) {
        PlatformToolkit.get().createBlockEntityType(
            ::UniversalScannerBlockEntity,
            Blocks.UNIVERSAL_SCANNER.get(),
        )
    }

    val ULTIMATE_SENSOR: Supplier<BlockEntityType<UltimateSensorBlockEntity>> = ModPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "ultimate_sensor"),
    ) {
        PlatformToolkit.get().createBlockEntityType(
            ::UltimateSensorBlockEntity,
            Blocks.ULTIMATE_SENSOR.get(),
        )
    }

    val ITEM_PEDESTAL: Supplier<BlockEntityType<ItemPedestalBlockEntity>> = ModPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "item_pedestal"),
    ) {
        PlatformToolkit.get().createBlockEntityType(
            ::ItemPedestalBlockEntity,
            Blocks.ITEM_PEDESTAL.get(),
        )
    }

    val MAP_PEDESTAL: Supplier<BlockEntityType<MapPedestalBlockEntity>> = ModPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "map_pedestal"),
    ) {
        PlatformToolkit.get().createBlockEntityType(
            ::MapPedestalBlockEntity,
            Blocks.MAP_PEDESTAL.get(),
        )
    }

    val DISPLAY_PEDESTAL: Supplier<BlockEntityType<DisplayPedestalBlockEntity>> = ModPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "display_pedestal"),
    ) {
        PlatformToolkit.get().createBlockEntityType(
            ::DisplayPedestalBlockEntity,
            Blocks.DISPLAY_PEDESTAL.get(),
        )
    }

    val REMOTE_OBSERVER: Supplier<BlockEntityType<RemoteObserverBlockEntity>> = ModPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "remote_observer"),
    ) {
        PlatformToolkit.get().createBlockEntityType(
            ::RemoteObserverBlockEntity,
            Blocks.REMOTE_OBSERVER.get(),
        )
    }

    val PERIPHERAL_PROXY: Supplier<BlockEntityType<PeripheralProxyBlockEntity>> = ModPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "peripheral_proxy"),
    ) {
        PlatformToolkit.get().createBlockEntityType(
            ::PeripheralProxyBlockEntity,
            Blocks.PERIPHERAL_PROXY.get(),
        )
    }

    val FLEXIBLE_REALITY_ANCHOR = ModPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "flexible_reality_anchor"),
    ) {
        PlatformToolkit.get().createBlockEntityType(
            ::FlexibleRealityAnchorBlockEntity,
            Blocks.FLEXIBLE_REALITY_ANCHOR.get(),
        )
    }

    val REALITY_FORGER: Supplier<BlockEntityType<RealityForgerBlockEntity>> = ModPlatform.registerBlockEntity(
        modId("reality_forger"),
    ) {
        PlatformToolkit.get().createBlockEntityType(
            ::RealityForgerBlockEntity,
            Blocks.REALITY_FORGER.get(),
        )
    }

    val RECIPE_REGISTRY: Supplier<BlockEntityType<RecipeRegistryBlockEntity>> = ModPlatform.registerBlockEntity(
        modId("recipe_registry"),
    ) {
        PlatformToolkit.get().createBlockEntityType(
            ::RecipeRegistryBlockEntity,
            Blocks.RECIPE_REGISTRY.get(),
        )
    }

    val INFORMATIVE_REGISTRY: Supplier<BlockEntityType<InformativeRegistryBlockEntity>> = ModPlatform.registerBlockEntity(
        modId("informative_registry"),
    ) {
        PlatformToolkit.get().createBlockEntityType(
            ::InformativeRegistryBlockEntity,
            Blocks.INFORMATIVE_REGISTRY.get(),
        )
    }

    val FLEXIBLE_STATUE = ModPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "flexible_statue"),
    ) {
        PlatformToolkit.get().createBlockEntityType(
            ::FlexibleStatueBlockEntity,
            Blocks.FLEXIBLE_STATUE.get(),
        )
    }

    val STATUE_WORKBENCH: Supplier<BlockEntityType<StatueWorkbenchBlockEntity>> = ModPlatform.registerBlockEntity(
        modId("statue_workbench"),
    ) {
        PlatformToolkit.get().createBlockEntityType(
            ::StatueWorkbenchBlockEntity,
            Blocks.STATUE_WORKBENCH.get(),
        )
    }

    val ENTITY_LINK: Supplier<BlockEntityType<EntityLinkBlockEntity>> = ModPlatform.registerBlockEntity(
        modId("entity_link"),
    ) {
        PlatformToolkit.get().createBlockEntityType(
            ::EntityLinkBlockEntity,
            Blocks.ENTITY_LINK.get(),
        )
    }

    val NETWORK_MANAGER: Supplier<BlockEntityType<NetworkManagerBlockEntity>> = ModPlatform.registerBlockEntity(
        modId("network_manager"),
    ) {
        PlatformToolkit.get().createBlockEntityType(
            ::NetworkManagerBlockEntity,
            Blocks.NETWORK_MANAGER.get(),
        )
    }

    fun doSomething() {}
}
