package site.siredvin.peripheralworks.common.events

import com.google.common.cache.CacheBuilder
import com.google.common.collect.EvictingQueue
import com.google.common.collect.MapMaker
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralworks.PeripheralWorksCore
import java.util.function.Consumer

object BlockStateUpdateEventBus {
    private const val EVENT_QUEUE_MAX_SIZE = 200

    data class BlockStateUpdateEvent(val pos: BlockPos, val previous: BlockState, val current: BlockState)

    private val listenedBlockPos: MutableSet<BlockPos> = mutableSetOf()
    private val hookedPlayers = MapMaker().weakKeys().concurrencyLevel(4).initialCapacity(1).makeMap<ServerPlayer, Boolean>()
    private var _lastEventID: Long = 0L

    private val eventQueue = EvictingQueue.create<Pair<Long, BlockStateUpdateEvent>>(EVENT_QUEUE_MAX_SIZE)

    val trackedBlocks: List<BlockPos>
        get() = listenedBlockPos.toList()

    @get:Synchronized
    val lastEventID: Long
        get() = _lastEventID

    @Synchronized
    fun postDebugLog() {
        CacheBuilder.newBuilder().weakKeys()
        PeripheralWorksCore.LOGGER.info("Current last event ID: $_lastEventID")
        PeripheralWorksCore.LOGGER.info("Current tracked pos: $listenedBlockPos")
    }

    fun addBlockPos(vararg pos: BlockPos) {
        listenedBlockPos.addAll(pos)
    }

    fun addBlockPos(pos: Collection<BlockPos>) {
        listenedBlockPos.addAll(pos)
    }

    fun removeBlockPos(vararg pos: BlockPos) {
        listenedBlockPos.removeAll(pos.toSet())
    }

    fun removeBlockPos(poses: Collection<BlockPos>) {
        listenedBlockPos.removeAll(poses.toSet())
    }

    @Synchronized
    fun putEventIntoQueue(id: Long, data: BlockStateUpdateEvent) {
        eventQueue.add(Pair(id, data))
        _lastEventID++
    }

    @Synchronized
    fun traverseEvents(lastConsumedID: Long, consumer: Consumer<BlockStateUpdateEvent>): Long {
        var consumedTracker = lastConsumedID
        for (message in eventQueue) {
            if (message != null) {
                if (message.first <= consumedTracker) {
                    continue
                }
                consumer.accept(message.second)
                consumedTracker = message.first
            }
        }
        return consumedTracker
    }

    fun togglePlayer(player: ServerPlayer) {
        if (hookedPlayers.contains(player)) {
            hookedPlayers.remove(player)
        } else {
            hookedPlayers[player] = true
        }
    }

    fun onBlockStateChange(pos: BlockPos, previous: BlockState, current: BlockState) {
        if (listenedBlockPos.contains(pos)) {
            putEventIntoQueue(lastEventID, BlockStateUpdateEvent(pos, previous, current))
            hookedPlayers.keys.forEach {
                it.sendSystemMessage(Component.literal("Block $pos changed"))
            }
        }
    }
}
