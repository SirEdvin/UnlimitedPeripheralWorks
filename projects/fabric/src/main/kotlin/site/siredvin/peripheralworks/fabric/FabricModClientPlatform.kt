package site.siredvin.peripheralworks.fabric

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import site.siredvin.peripheralworks.xplat.ModClientInternalPlatform
import java.util.function.Supplier

object FabricModClientPlatform : ModClientInternalPlatform {
    override fun registerBlockEntityRendererCallback(sup: Supplier<List<Pair<BlockEntityType<BlockEntity>, BlockEntityRendererProvider<BlockEntity>>>>) {
        sup.get().forEach {
            BlockEntityRenderers.register(it.first, it.second)
        }
    }
}
