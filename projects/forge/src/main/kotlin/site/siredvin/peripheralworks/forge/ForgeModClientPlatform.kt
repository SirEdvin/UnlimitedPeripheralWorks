package site.siredvin.peripheralworks.forge

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import site.siredvin.peripheralworks.client.configurator.NetworkManagerScreenEntry
import site.siredvin.peripheralworks.client.configurator.TargetRenderSettingsScreenEntry
import site.siredvin.peripheralworks.xplat.ModClientInternalPlatform
import java.util.function.Supplier

object ForgeModClientPlatform : ModClientInternalPlatform {
    val BLOCK_ENTITY_RENDERER_SUPPLIER = mutableListOf<Supplier<List<Pair<BlockEntityType<BlockEntity>, BlockEntityRendererProvider<BlockEntity>>>>>()

    override fun openNetworkManagerScreen(pos: BlockPos) = NetworkManagerScreenEntry.open(pos)
    override fun openTargetRenderSettingsScreen(pos: BlockPos) = TargetRenderSettingsScreenEntry.open(pos)

    override fun registerBlockEntityRendererCallback(sup: Supplier<List<Pair<BlockEntityType<BlockEntity>, BlockEntityRendererProvider<BlockEntity>>>>) {
        BLOCK_ENTITY_RENDERER_SUPPLIER.add(sup)
    }
}
