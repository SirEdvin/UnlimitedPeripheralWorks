package site.siredvin.peripheralworks.integrations.kubejs

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry
import dev.latvian.mods.kubejs.registry.RegistryObjectStorage
import dev.latvian.mods.kubejs.util.ID
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.loading.FMLEnvironment
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.client.renderer.PedestalTileRenderer
import site.siredvin.peripheralworks.common.blockentity.ItemPedestalBlockEntity
import site.siredvin.peripheralworks.xplat.ModClientPlatform

class Plugin : KubeJSPlugin {
    init {
        PeripheralWorksCore.logger.info("Oh, how cute, upw kubejs integration was just created.")
    }
    override fun init() {
        PeripheralWorksCore.logger.info("Oh, how cute, upw kubejs integration started loading!")
        if (FMLEnvironment.dist == Dist.CLIENT) {
            registerRenderers()
        }
        PeripheralWorksCore.logger.info("Oh, how cute, upw kubejs integration loaded just fine!")
    }

    override fun registerBuilderTypes(registry: BuilderTypeRegistry) {
        registry.of(Registries.BLOCK) {
            it.add(ID.kjs("item_pedestal"), ItemPedestalBuilder::class.java, ::ItemPedestalBuilder)
            it.add(ID.kjs("display_pedestal"), DisplayPedestalBuilder::class.java, ::DisplayPedestalBuilder)
        }
    }

    private fun registerRenderers() {
        ModClientPlatform.registerBlockEntityRendererCallback(
            {
                val output = mutableListOf<Pair<BlockEntityType<BlockEntity>, BlockEntityRendererProvider<BlockEntity>>>()
                RegistryObjectStorage.BLOCK_ENTITY.iterator().forEach {
                    if (it is PedestalBlockEntityBuilder) {
                        @Suppress("UNCHECKED_CAST")
                        output.add(
                            Pair(
                                it.get() as BlockEntityType<BlockEntity>,
                                BlockEntityRendererProvider {
                                    @Suppress("UNCHECKED_CAST")
                                    PedestalTileRenderer<ItemPedestalBlockEntity>() as BlockEntityRenderer<BlockEntity>
                                },
                            ),
                        )
                    }
                }
                return@registerBlockEntityRendererCallback output
            },
        )
    }
}
