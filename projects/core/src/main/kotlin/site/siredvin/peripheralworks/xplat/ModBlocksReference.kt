package site.siredvin.peripheralworks.xplat

import net.minecraft.world.level.block.Block

interface ModBlocksReference {
    companion object {
        private var _IMPL: ModBlocksReference? = null

        fun configure(impl: ModBlocksReference) {
            _IMPL = impl
        }

        fun get(): ModBlocksReference {
            if (_IMPL == null) {
                throw IllegalStateException("You should init PeripheralWorks Platform first")
            }
            return _IMPL!!
        }
    }

    val wiredModem: Block
    val cable: Block
}
