package site.siredvin.peripheralworks.integrations.kubejs

import dev.latvian.mods.kubejs.KubeJSPlugin
import dev.latvian.mods.kubejs.registry.RegistryInfo
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.client.renderer.PedestalTileRenderer
import site.siredvin.peripheralworks.common.blockentity.ItemPedestalBlockEntity
import site.siredvin.peripheralworks.xplat.ModClientPlatform

class Plugin : KubeJSPlugin() {
    init {
        PeripheralWorksCore.logger.info("Oh, how cute, upw kubejs integration was just created.")
    }
    override fun init() {
        PeripheralWorksCore.logger.info("Oh, how cute, upw kubejs integration started loading!")
        RegistryInfo.BLOCK.addType("item_pedestal", ItemPedestalBuilder::class.java, ::ItemPedestalBuilder)
        RegistryInfo.BLOCK.addType("display_pedestal", DisplayPedestalBuilder::class.java, ::DisplayPedestalBuilder)
        PeripheralWorksCore.logger.info("Oh, how cute, upw kubejs integration loaded just fine!")
    }

    override fun clientInit() {
        ModClientPlatform.registerBlockEntityRendererCallback(
            {
                val output = mutableListOf<Pair<BlockEntityType<BlockEntity>, BlockEntityRendererProvider<BlockEntity>>>()
                RegistryInfo.BLOCK_ENTITY_TYPE.iterator().forEach {
                    if (it is PedestalBlockEntityBuilder) {
                        @Suppress("UNCHECKED_CAST")
                        val type = RegistryInfo.BLOCK_ENTITY_TYPE.getValue(it.id)
                        if (type != null) {
                            output.add(
                                Pair(
                                    type as BlockEntityType<BlockEntity>,
                                    BlockEntityRendererProvider {
                                        @Suppress("UNCHECKED_CAST")
                                        PedestalTileRenderer<ItemPedestalBlockEntity>() as BlockEntityRenderer<BlockEntity>
                                    },
                                ),
                            )
                        } else {
                            PeripheralWorksCore.logger.error("Block entity type for pedestal builder: ${it.id} is None for some reason")
                        }
                    }
                }
                return@registerBlockEntityRendererCallback output
            },
        )
    }
}
