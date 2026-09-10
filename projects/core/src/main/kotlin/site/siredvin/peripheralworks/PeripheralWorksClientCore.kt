package site.siredvin.peripheralworks

import dan200.computercraft.api.client.turtle.TurtleUpgradeModeller
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.upgrades.UpgradeType
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
import site.siredvin.peripheralworks.common.setup.ModTurtleUpgrades
import site.siredvin.peripheralworks.computercraft.peripherals.HologramProjectorPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UltimateSensorPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UniversalScannerPeripheral
import site.siredvin.peripheralworks.xplat.ModClientInternalPlatform
import site.siredvin.peripheralworks.xplat.ModClientPlatform
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Supplier

object PeripheralWorksClientCore {
    private val HOOKS = mutableListOf<Runnable>()
    val EXTRA_MODELS = arrayOf(
        "turtle/universal_scanner_left",
        "turtle/universal_scanner_right",
        "turtle/ultimate_sensor_left",
        "turtle/ultimate_sensor_right",
        "turtle/hologram_projector_left",
        "turtle/hologram_projector_right",
    )
    val EXTRA_TURTLE_MODEL_PROVIDERS: MutableList<Supplier<Pair<UpgradeType<out ITurtleUpgrade>, TurtleUpgradeModeller<ITurtleUpgrade>>>> = mutableListOf()
    private var inited: Boolean = false

    @Suppress("UNCHECKED_CAST")
    val EXTRA_BLOCK_ENTITY_RENDERERS: MutableList<Supplier<BlockEntityType<BlockEntity>>> = mutableListOf(
        BlockEntityTypes.ITEM_PEDESTAL as Supplier<BlockEntityType<BlockEntity>>,
        BlockEntityTypes.MAP_PEDESTAL as Supplier<BlockEntityType<BlockEntity>>,
        BlockEntityTypes.DISPLAY_PEDESTAL as Supplier<BlockEntityType<BlockEntity>>,
        BlockEntityTypes.PERIPHERAL_PROXY as Supplier<BlockEntityType<BlockEntity>>,
//        BlockEntityTypes.UNIVERSAL_SCANNER as Supplier<BlockEntityType<BlockEntity>>,
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
//        if (type == BlockEntityTypes.UNIVERSAL_SCANNER.get()) {
//            return BlockEntityRendererProvider { UniversalScannerRenderer() } as BlockEntityRendererProvider<BlockEntity>
//        }
        throw IllegalArgumentException("There is no extra renderer for $type")
    }

    @Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
    fun registerExtraModels(register: Consumer<ResourceLocation>) {
        EXTRA_MODELS.forEach { register.accept(ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, it)) }
    }

    fun addHook(hook: Runnable) {
        if (!inited) {
            HOOKS.add(hook)
        } else {
            hook.run()
        }
    }

    fun onModelRegister(consumer: BiConsumer<UpgradeType<out ITurtleUpgrade>, TurtleUpgradeModeller<ITurtleUpgrade>>) {
        consumer.accept(
            ModTurtleUpgrades.PERIPHERALIUM_HUB.get(),
            ScaledItemModeller(0.5f),
        )
        consumer.accept(
            ModTurtleUpgrades.NETHERITE_PERIPHERALIUM_HUB.get(),
            ScaledItemModeller(0.5f),
        )
        consumer.accept(
            ModTurtleUpgrades.UNIVERSAL_SCANNER.get(),
            TurtleUpgradeModeller.sided(
                ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "turtle/${UniversalScannerPeripheral.UPGRADE_ID.path}_left"),
                ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "turtle/${UniversalScannerPeripheral.UPGRADE_ID.path}_right"),
            ),
        )
        consumer.accept(
            ModTurtleUpgrades.ULTIMATE_SENSOR.get(),
            TurtleUpgradeModeller.sided(
                ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "turtle/${UltimateSensorPeripheral.UPGRADE_ID.path}_left"),
                ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "turtle/${UltimateSensorPeripheral.UPGRADE_ID.path}_right"),
            ),
        )
        consumer.accept(
            ModTurtleUpgrades.HOLOGRAM_PROJECTOR.get(),
            TurtleUpgradeModeller.sided(
                ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "turtle/${HologramProjectorPeripheral.UPGRADE_ID.path}_left"),
                ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "turtle/${HologramProjectorPeripheral.UPGRADE_ID.path}_right"),
            ),
        )
        EXTRA_TURTLE_MODEL_PROVIDERS.forEach {
            val pair = it.get()
            consumer.accept(pair.first, pair.second)
        }
    }

    fun onInit() {
        if (inited) return
        inited = true
        HOOKS.forEach(Runnable::run)
    }

    fun configure(clientPlatform: ModClientInternalPlatform) {
        ModClientPlatform.configure(clientPlatform)
    }
}
