package site.siredvin.peripheralworks.common.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component
import site.siredvin.peripheralworks.common.events.BlockStateUpdateEventBus

object DebugCommands {
    private const val COMMAND = "upw"
    private const val ADMIN_PERMISSION_LEVEL = 3
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            literal(COMMAND).requires { it.hasPermission(ADMIN_PERMISSION_LEVEL) }.then(
                literal("printTrackedBlocks").executes {
                    val player = it.source.player ?: return@executes 0
                    BlockStateUpdateEventBus.trackedBlocks.forEach { pos ->
                        player.sendSystemMessage(Component.literal(pos.toString()))
                    }
                    0
                },
            ),
        )
        dispatcher.register(
            literal(COMMAND).requires { it.hasPermission(ADMIN_PERMISSION_LEVEL) }.then(
                literal("toggleTracking").executes {
                    val player = it.source.player ?: return@executes 0
                    BlockStateUpdateEventBus.togglePlayer(player)
                    0
                },
            ),
        )
        dispatcher.register(
            literal(COMMAND).requires { it.hasPermission(ADMIN_PERMISSION_LEVEL) }.then(
                literal("inspectItem").executes {
                    val player = it.source.player ?: return@executes 0
                    val stack = player.mainHandItem
                    player.displayClientMessage(
                        Component.literal(
                            "Item Data: ${stack.tag}",
                        ),
                        false,
                    )
                    0
                },
            ),
        )
    }
}
