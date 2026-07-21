package site.siredvin.peripheralworks.networking

import net.minecraft.server.level.ServerPlayer

fun interface ServerNetworkContext {
    fun getSender(): ServerPlayer
}
