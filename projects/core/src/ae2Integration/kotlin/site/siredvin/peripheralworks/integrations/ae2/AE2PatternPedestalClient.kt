package site.siredvin.peripheralworks.integrations.ae2

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import site.siredvin.peripheralworks.client.renderer.PedestalTileRenderer
import site.siredvin.peripheralworks.xplat.ModClientPlatform

internal object AE2PatternPedestalClient {
    @Suppress("UNCHECKED_CAST")
    fun register() {
        ModClientPlatform.registerBlockEntityRendererCallback {
            listOf(
                Pair(
                    Registration.PATTERN_PEDESTAL_BLOCK_ENTITY.get() as BlockEntityType<BlockEntity>,
                    BlockEntityRendererProvider { PedestalTileRenderer<AE2PatternPedestalBlockEntity>() as BlockEntityRenderer<BlockEntity> },
                ),
            )
        }
    }
}
