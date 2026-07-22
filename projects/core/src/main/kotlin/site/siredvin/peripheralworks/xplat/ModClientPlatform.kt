package site.siredvin.peripheralworks.xplat

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import java.util.function.Supplier

object ModClientPlatform {
    private var impl: ModClientInternalPlatform? = null
    private val CONFIGURATION_HOOKS: MutableList<Runnable> = mutableListOf()

    fun configure(impl: ModClientInternalPlatform) {
        this.impl = impl
        CONFIGURATION_HOOKS.forEach(Runnable::run)
    }

    val isConfigured: Boolean
        get() = impl != null

    val baseInnerPlatform: ModClientInternalPlatform
        get() {
            if (impl == null) {
                throw IllegalStateException("You should configure upw ModClientPlatform first")
            }
            return impl!!
        }

    fun registerBlockEntityRendererCallback(sup: Supplier<List<Pair<BlockEntityType<BlockEntity>, BlockEntityRendererProvider<BlockEntity>>>>) {
        if (!isConfigured) {
            this.CONFIGURATION_HOOKS.add({ this.registerBlockEntityRendererCallback(sup) })
        } else {
            this.baseInnerPlatform.registerBlockEntityRendererCallback(sup)
        }
    }

    fun openNetworkManagerScreen(pos: BlockPos) = baseInnerPlatform.openNetworkManagerScreen(pos)
}
