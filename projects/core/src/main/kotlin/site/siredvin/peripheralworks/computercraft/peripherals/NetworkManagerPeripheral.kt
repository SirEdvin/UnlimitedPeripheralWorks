package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IComputerAccess
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.utils.extractPosition
import site.siredvin.tweakium.modules.peripheral.OwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation

class NetworkManagerPeripheral(private val be: NetworkManagerBlockEntity) :
    OwnedPeripheral<BlockEntityPeripheralOwner<NetworkManagerBlockEntity>>(
        TYPE,
        BlockEntityPeripheralOwner(be),
    ) {

    companion object {
        const val TYPE = "network_manager"
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enableNetworkManager

    @LuaFunction(mainThread = true)
    fun getGroups(): List<String> = be.peripheralGroups.keys.toList()

    @LuaFunction(mainThread = true)
    fun addGroup(group: String): MethodResult = when (be.createGroup(group)) {
        NetworkManagerBlockEntity.GroupOperationResult.SUCCESS -> MethodResult.of(true)
        NetworkManagerBlockEntity.GroupOperationResult.GROUP_EXISTS -> MethodResult.of(false, "Such group already exists")
        else -> MethodResult.of(false, "Invalid group name")
    }

    @LuaFunction(mainThread = true)
    fun removeGroup(group: String): MethodResult {
        if (!be.peripheralGroups.contains(group)) return MethodResult.of(false, "Group does not exists")
        if (be.peripheralGroups[group]!!.peripherals.any { be.peripherals.contains(it) }) return MethodResult.of(false, "Group is not empty")
        be.deleteGroup(group)
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun add(group: String, peripheral: String): MethodResult = when (be.setGroupMembership(group, peripheral, true)) {
        NetworkManagerBlockEntity.GroupOperationResult.SUCCESS -> MethodResult.of(true)
        NetworkManagerBlockEntity.GroupOperationResult.PERIPHERAL_MISSING -> MethodResult.of(false, "There is no such peripheral")
        NetworkManagerBlockEntity.GroupOperationResult.GROUP_MISSING -> MethodResult.of(false, "There is no such group")
        else -> MethodResult.of(false, "Peripheral already in the group")
    }

    @LuaFunction(mainThread = true)
    fun remove(group: String, peripheral: String): MethodResult = when (be.setGroupMembership(group, peripheral, false)) {
        NetworkManagerBlockEntity.GroupOperationResult.SUCCESS -> MethodResult.of(true)
        NetworkManagerBlockEntity.GroupOperationResult.PERIPHERAL_MISSING -> MethodResult.of(false, "There is no such peripheral")
        NetworkManagerBlockEntity.GroupOperationResult.GROUP_MISSING -> MethodResult.of(false, "There is no such group")
        else -> MethodResult.of(false, "Peripheral not in the group")
    }

    @LuaFunction(mainThread = true)
    fun setGroupColor(group: String, color: Int): MethodResult = when (be.setGroupColor(group, color)) {
        NetworkManagerBlockEntity.GroupOperationResult.SUCCESS -> MethodResult.of(true)
        NetworkManagerBlockEntity.GroupOperationResult.GROUP_MISSING -> MethodResult.of(false, "There is no such group")
        else -> MethodResult.of(false, "Invalid color")
    }

    @LuaFunction(mainThread = true)
    fun getGroupColor(group: String): MethodResult {
        if (!be.peripheralGroups.contains(group)) return MethodResult.of(false, "There is no such group")
        return MethodResult.of(be.peripheralGroups[group]!!.color)
    }

    @LuaFunction(mainThread = true)
    fun get(group: String): MethodResult {
        val group = be.peripheralGroups[group] ?: return MethodResult.of()
        return MethodResult.of(
            *group.peripherals.filter { be.peripherals.contains(it) }.toTypedArray(),
        )
    }

    @LuaFunction(mainThread = true)
    fun getDistanceBetween(computer: IComputerAccess, firstName: String, secondName: String): MethodResult {
        val first = computer.getAvailablePeripheral(firstName) ?: return MethodResult.of(null, "Cannot find first peripheral")
        val second = computer.getAvailablePeripheral(secondName) ?: return MethodResult.of(null, "Cannot find second peripheral")
        val firstPos = first.extractPosition() ?: return MethodResult.of(null, "Cannot determine first target position")
        val secondPos = second.extractPosition() ?: return MethodResult.of(null, "Cannot determine second target position")
        val firstBlockState = peripheralOwner.level!!.getBlockState(firstPos)
        val distance = BlockPos(firstPos.x - secondPos.x, firstPos.y - secondPos.y, firstPos.z - secondPos.z)
        val facing = if (firstBlockState.properties.contains(BlockStateProperties.HORIZONTAL_FACING)) {
            firstBlockState.getValue(BlockStateProperties.HORIZONTAL_FACING)
        } else {
            Direction.EAST
        }
        return MethodResult.of(LuaRepresentation.forBlockPos(distance, facing.opposite, BlockPos(0, 0, 0)))
    }
}
