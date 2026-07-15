package site.siredvin.peripheralworks.xplat

import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block

interface ModBlocksReference {
    companion object {
        private var impl: ModBlocksReference? = null

        fun configure(impl: ModBlocksReference) {
            this.impl = impl
        }

        fun get(): ModBlocksReference {
            if (impl == null) {
                throw IllegalStateException("You should init PeripheralWorks Platform first")
            }
            return impl!!
        }
    }

    val wiredModem: Block
    val cable: Block
    val cableItem: Item
}
