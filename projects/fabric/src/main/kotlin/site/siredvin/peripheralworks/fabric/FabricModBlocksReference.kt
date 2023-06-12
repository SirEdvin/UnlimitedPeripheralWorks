package site.siredvin.peripheralworks.fabric

import dan200.computercraft.shared.ModRegistry
import net.minecraft.world.level.block.Block
import site.siredvin.peripheralworks.xplat.ModBlocksReference

object FabricModBlocksReference: ModBlocksReference {
    override val wiredModem: Block
        get() = ModRegistry.Blocks.WIRED_MODEM_FULL.get()
    override val cable: Block
        get() = ModRegistry.Blocks.CABLE.get()
}