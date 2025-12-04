package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IComputerAccess
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
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
    fun addGroup(group: String): MethodResult {
        if (be.peripheralGroups.contains(group)) return MethodResult.of(false, "Such group already exists")
        be.peripheralGroups[group] = NetworkManagerBlockEntity.PeripheralGroup()
        be.setChanged()
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun removeGroup(group: String): MethodResult {
        if (!be.peripheralGroups.contains(group)) return MethodResult.of(false, "Group does not exists")
        if (be.peripheralGroups[group]!!.peripherals.any { be.peripherals.contains(it) }) return MethodResult.of(false, "Group is not empty")
        be.peripheralGroups.remove(group)
        be.setChanged()
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun add(group: String, peripheral: String): MethodResult {
        if (!be.peripherals.contains(peripheral)) return MethodResult.of(false, "There is no such peripheral")
        val groupInstance = be.peripheralGroups[group] ?: return MethodResult.of(false, "There is no such group")
        if (groupInstance.peripherals.contains(peripheral)) {
            return MethodResult.of(false, "Peripheral already in the group")
        }
        groupInstance.peripherals.add(peripheral)
        queueEvent("network_manager_group_change", group, "added", peripheral)
        be.pushData()
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun remove(group: String, peripheral: String): MethodResult {
        if (!be.peripherals.contains(peripheral)) return MethodResult.of(false, "There is no such peripheral")
        val groupInstance = be.peripheralGroups[group] ?: return MethodResult.of(false, "There is no such group")
        if (!groupInstance.peripherals.contains(peripheral)) {
            return MethodResult.of(false, "Peripheral not in the group")
        }
        groupInstance.peripherals.remove(peripheral)
        queueEvent("network_manager_group_change", group, "removed", peripheral)
        be.pushData()
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun setGroupColor(group: String, color: Int): MethodResult {
        if (!be.peripheralGroups.contains(group)) return MethodResult.of(false, "There is no such group")
        be.peripheralGroups[group]!!.color = color
        be.pushData()
        return MethodResult.of(true)
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
        val firstTarget = first.target
        val secondTarget = second.target
        val firstPos = firstTarget as? BlockPos
            ?: if (firstTarget is BlockEntity) {
                firstTarget.blockPos
            } else {
                return MethodResult.of(null, "Cannot determine first target position")
            }
        val secondPos = secondTarget as? BlockPos
            ?: if (secondTarget is BlockEntity) {
                secondTarget.blockPos
            } else {
                return MethodResult.of(null, "Cannot determine second target position")
            }
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
