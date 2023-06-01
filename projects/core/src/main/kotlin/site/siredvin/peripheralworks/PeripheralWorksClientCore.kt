package site.siredvin.peripheralworks

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import site.siredvin.peripheralworks.client.renderer.PedestalTileRenderer
import site.siredvin.peripheralworks.client.renderer.PeripheralProxyRenderer
import site.siredvin.peripheralworks.common.blockentity.DisplayPedestalBlockEntity
import site.siredvin.peripheralworks.common.blockentity.ItemPedestalBlockEntity
import site.siredvin.peripheralworks.common.blockentity.MapPedestalBlockEntity
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import java.util.function.Consumer
import java.util.function.Supplier

object PeripheralWorksClientCore {
    private val CLIENT_HOOKS: MutableList<Runnable> = mutableListOf()
    private var initialized: Boolean = false

    private val EXTRA_MODELS = arrayOf(
        "turtle/universal_scanner_left",
        "turtle/universal_scanner_right",
        "turtle/ultimate_sensor_left",
        "turtle/ultimate_sensor_right",
    )

    val EXTRA_BLOCK_ENTITY_RENDERERS: Array<Supplier<BlockEntityType<BlockEntity>>> = arrayOf(
        BlockEntityTypes.ITEM_PEDESTAL as Supplier<BlockEntityType<BlockEntity>>,
        BlockEntityTypes.MAP_PEDESTAL as Supplier<BlockEntityType<BlockEntity>>,
        BlockEntityTypes.DISPLAY_PEDESTAL as Supplier<BlockEntityType<BlockEntity>>,
        BlockEntityTypes.PERIPHERAL_PROXY as Supplier<BlockEntityType<BlockEntity>>,
    )

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

    fun registerHook(it: Runnable) {
        if (!initialized) {
            CLIENT_HOOKS.add(it)
        } else {
            it.run()
        }
    }

    fun onInit() {
        CLIENT_HOOKS.forEach { it.run() }
        initialized = true
    }
}
