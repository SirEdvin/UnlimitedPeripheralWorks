package site.siredvin.peripheralworks

import dan200.computercraft.api.client.ComputerCraftAPIClient
import dan200.computercraft.api.client.turtle.TurtleUpgradeModeller
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import site.siredvin.peripheralworks.client.renderer.PedestalTileRenderer
import site.siredvin.peripheralworks.client.renderer.PeripheralProxyRenderer
import site.siredvin.peripheralworks.client.turtle.ScaledItemModeller
import site.siredvin.peripheralworks.common.blockentity.DisplayPedestalBlockEntity
import site.siredvin.peripheralworks.common.blockentity.ItemPedestalBlockEntity
import site.siredvin.peripheralworks.common.blockentity.MapPedestalBlockEntity
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.common.setup.TurtleUpgradeSerializers
import site.siredvin.peripheralworks.computercraft.peripherals.UltimateSensorPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UniversalScannerPeripheral
import java.util.function.Consumer
import java.util.function.Supplier

object PeripheralWorksClientCore {
    private val HOOKS = mutableListOf<Runnable>()
    private val EXTRA_MODELS = arrayOf(
        "turtle/universal_scanner_left",
        "turtle/universal_scanner_right",
        "turtle/ultimate_sensor_left",
        "turtle/ultimate_sensor_right",
    )
    private var inited: Boolean = false

    @Suppress("UNCHECKED_CAST")
    val EXTRA_BLOCK_ENTITY_RENDERERS: Array<Supplier<BlockEntityType<BlockEntity>>> = arrayOf(
        BlockEntityTypes.ITEM_PEDESTAL as Supplier<BlockEntityType<BlockEntity>>,
        BlockEntityTypes.MAP_PEDESTAL as Supplier<BlockEntityType<BlockEntity>>,
        BlockEntityTypes.DISPLAY_PEDESTAL as Supplier<BlockEntityType<BlockEntity>>,
        BlockEntityTypes.PERIPHERAL_PROXY as Supplier<BlockEntityType<BlockEntity>>,
    )

    @Suppress("UNCHECKED_CAST")
    fun getBlockEntityRendererProvider(type: BlockEntityType<BlockEntity>): BlockEntityRendererProvider<BlockEntity> {
        if (type == BlockEntityTypes.ITEM_PEDESTAL.get()) {
            return BlockEntityRendererProvider { PedestalTileRenderer<ItemPedestalBlockEntity>() } as BlockEntityRendererProvider<BlockEntity>
        }
        if (type == BlockEntityTypes.MAP_PEDESTAL.get()) {
            return BlockEntityRendererProvider { PedestalTileRenderer<MapPedestalBlockEntity>() } as BlockEntityRendererProvider<BlockEntity>
        }
        if (type == BlockEntityTypes.DISPLAY_PEDESTAL.get()) {
            return BlockEntityRendererProvider { PedestalTileRenderer<DisplayPedestalBlockEntity>() } as BlockEntityRendererProvider<BlockEntity>
        }
        if (type == BlockEntityTypes.PERIPHERAL_PROXY.get()) {
            return BlockEntityRendererProvider { PeripheralProxyRenderer() } as BlockEntityRendererProvider<BlockEntity>
        }
        throw IllegalArgumentException("There is no extra renderer for $type")
    }

    fun registerExtraModels(register: Consumer<ResourceLocation>) {
        EXTRA_MODELS.forEach { register.accept(ResourceLocation(PeripheralWorksCore.MOD_ID, it)) }
    }

    fun addHook(hook: Runnable) {
        if (!inited) {
            HOOKS.add(hook)
        } else {
            hook.run()
        }
    }

    fun onInit() {
        inited = true
        HOOKS.forEach(Runnable::run)
        ComputerCraftAPIClient.registerTurtleUpgradeModeller(
            TurtleUpgradeSerializers.PERIPHERALIUM_HUB.get(),
            ScaledItemModeller(0.5f),
        )
        ComputerCraftAPIClient.registerTurtleUpgradeModeller(
            TurtleUpgradeSerializers.NETHERITE_PERIPHERALIUM_HUB.get(),
            ScaledItemModeller(0.5f),
        )
        ComputerCraftAPIClient.registerTurtleUpgradeModeller(
            TurtleUpgradeSerializers.UNIVERSAL_SCANNER.get(),
            TurtleUpgradeModeller.sided(
                ResourceLocation(PeripheralWorksCore.MOD_ID, "turtle/${UniversalScannerPeripheral.UPGRADE_ID.path}_left"),
                ResourceLocation(PeripheralWorksCore.MOD_ID, "turtle/${UniversalScannerPeripheral.UPGRADE_ID.path}_right"),
            ),
        )
        ComputerCraftAPIClient.registerTurtleUpgradeModeller(
            TurtleUpgradeSerializers.ULTIMATE_SENSOR.get(),
            TurtleUpgradeModeller.sided(
                ResourceLocation(PeripheralWorksCore.MOD_ID, "turtle/${UltimateSensorPeripheral.UPGRADE_ID.path}_left"),
                ResourceLocation(PeripheralWorksCore.MOD_ID, "turtle/${UltimateSensorPeripheral.UPGRADE_ID.path}_right"),
            ),
        )
    }
}
