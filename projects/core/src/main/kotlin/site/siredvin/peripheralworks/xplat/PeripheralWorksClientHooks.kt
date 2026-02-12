package site.siredvin.peripheralworks.xplat

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import java.util.function.Supplier

object PeripheralWorksClientHooks {
    val BLOCK_ENTITY_RENDERER_SUPPLIER = mutableListOf<Supplier<List<Pair<BlockEntityType<BlockEntity>, BlockEntityRendererProvider<BlockEntity>>>>>()
}
