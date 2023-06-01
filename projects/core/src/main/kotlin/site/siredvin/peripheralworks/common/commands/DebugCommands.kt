package site.siredvin.peripheralworks.common.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component
import site.siredvin.peripheralworks.common.events.BlockStateUpdateEventBus

object DebugCommands {
    private const val COMMAND = "upw"
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(literal(COMMAND).then(literal("printTrackedBlocks").executes {
            val player = it.source.player ?: return@executes 0
            BlockStateUpdateEventBus.trackedBlocks.forEach {pos ->
                player.sendSystemMessage(Component.literal(pos.toString()))
            }
            0
        }))
        dispatcher.register(literal(COMMAND).then(literal("toggleTracking").executes {
            val player = it.source.player ?: return@executes 0
            BlockStateUpdateEventBus.togglePlayer(player)
            0
        }))
    }
}