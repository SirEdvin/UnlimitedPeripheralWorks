package site.siredvin.peripheralworks.networking

import dan200.computercraft.shared.computer.menu.ComputerMenu
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu

abstract class ComputerServerMessage : NetworkMessage<ServerNetworkContext> {
    private val containerId: Int

    protected constructor(menu: AbstractContainerMenu) {
        containerId = menu.containerId
    }

    constructor(buffer: FriendlyByteBuf) {
        containerId = buffer.readVarInt()
    }

    override fun write(buf: FriendlyByteBuf) {
        buf.writeVarInt(containerId)
    }

    override fun handle(context: ServerNetworkContext) {
        val player: Player = context.getSender()
        if (player.containerMenu.containerId == containerId && player.containerMenu is ComputerMenu) {
            handle(context, player.containerMenu as ComputerMenu)
        }
    }

    protected abstract fun handle(context: ServerNetworkContext, container: ComputerMenu)
}