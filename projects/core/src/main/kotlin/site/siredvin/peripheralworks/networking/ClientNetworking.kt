package site.siredvin.peripheralworks.networking

import net.minecraft.client.Minecraft
import site.siredvin.peripheralworks.xplat.ModPlatform

object ClientNetworking {
    fun sendToServer(message: NetworkMessage<ServerNetworkContext>) {
        val connection = Minecraft.getInstance().connection
        connection?.send(ModPlatform.baseInnerPlatform.createServerPacket(message))
    }
}