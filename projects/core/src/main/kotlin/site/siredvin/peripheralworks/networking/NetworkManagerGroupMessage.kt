package site.siredvin.peripheralworks.networking

import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
import site.siredvin.peripheralworks.common.item.UltimateConfigurator
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.peripheralworks.subsystem.configurator.NetworkManagerMode

class NetworkManagerGroupMessage(
    private val pos: BlockPos,
    private val operation: Operation,
    private val group: String,
    private val value: String = "",
    private val color: Int = -1,
    private val present: Boolean = false,
    private val expectedPresent: Boolean = false,
) : NetworkMessage<ServerNetworkContext> {
    enum class Operation { SELECT, CREATE, RENAME, DELETE, COLOR, MEMBERSHIP }

    constructor(buf: FriendlyByteBuf) : this(
        buf.readBlockPos(),
        buf.readEnum(Operation::class.java),
        buf.readUtf(NetworkManagerBlockEntity.MAX_GROUP_NAME_LENGTH),
        buf.readUtf(MAX_VALUE_LENGTH),
        buf.readInt(),
        buf.readBoolean(),
        buf.readBoolean(),
    )

    override fun type(): MessageType<*> = NetworkMessages.NETWORK_MANAGER_GROUP

    override fun write(buf: FriendlyByteBuf) {
        buf.writeBlockPos(pos)
        buf.writeEnum(operation)
        buf.writeUtf(group, NetworkManagerBlockEntity.MAX_GROUP_NAME_LENGTH)
        buf.writeUtf(value, MAX_VALUE_LENGTH)
        buf.writeInt(color)
        buf.writeBoolean(present)
        buf.writeBoolean(expectedPresent)
    }

    override fun handle(context: ServerNetworkContext) {
        val player = context.getSender()
        val stack = player.mainHandItem
        val activeMode = (stack.item as? UltimateConfigurator)?.getActiveMode(stack)
        if (!stack.`is`(Items.ULTIMATE_CONFIGURATOR.get()) || activeMode?.first?.modeID != NetworkManagerMode.modeID || activeMode.second != pos || !UltimateConfigurator.isActiveModeDimension(stack, player.level()) || !player.level().isLoaded(pos)) {
            player.displayClientMessage(ModText.NETWORK_MANAGER_REQUEST_REJECTED.text, true)
            return
        }
        val manager = player.level().getBlockEntity(pos) as? NetworkManagerBlockEntity
        if (manager == null) {
            player.displayClientMessage(ModText.NETWORK_MANAGER_UNAVAILABLE.text, true)
            return
        }

        val selected = UltimateConfigurator.getSelectedNetworkGroup(stack)
        val result = when (operation) {
            Operation.SELECT -> if (manager.peripheralGroups.containsKey(group)) NetworkManagerBlockEntity.GroupOperationResult.SUCCESS else NetworkManagerBlockEntity.GroupOperationResult.GROUP_MISSING
            Operation.CREATE -> manager.createGroup(group)
            Operation.RENAME -> if (selected == group) manager.renameGroup(group, value) else NetworkManagerBlockEntity.GroupOperationResult.GROUP_MISSING
            Operation.DELETE -> if (selected == group) manager.deleteGroup(group) else NetworkManagerBlockEntity.GroupOperationResult.GROUP_MISSING
            Operation.COLOR -> if (selected == group) manager.setGroupColor(group, color) else NetworkManagerBlockEntity.GroupOperationResult.GROUP_MISSING
            Operation.MEMBERSHIP -> if (selected == group) manager.setGroupMembership(group, value, present, expectedPresent) else NetworkManagerBlockEntity.GroupOperationResult.GROUP_MISSING
        }
        if (result == NetworkManagerBlockEntity.GroupOperationResult.SUCCESS) {
            when (operation) {
                Operation.SELECT, Operation.CREATE -> UltimateConfigurator.setSelectedNetworkGroup(stack, group)
                Operation.RENAME -> UltimateConfigurator.setSelectedNetworkGroup(stack, value)
                Operation.DELETE -> UltimateConfigurator.clearSelectedNetworkGroup(stack)
                else -> Unit
            }
        }
        player.displayClientMessage(if (result == NetworkManagerBlockEntity.GroupOperationResult.SUCCESS) ModText.NETWORK_MANAGER_REQUEST_SUCCEEDED.text else ModText.NETWORK_MANAGER_REQUEST_FAILED.format(result.name.lowercase()), true)
    }

    companion object {
        private const val MAX_VALUE_LENGTH = 128
    }
}
