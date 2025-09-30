package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.tweakium.modules.peripheral.OwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.owner.BlockEntityPeripheralOwner

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
        be.pushData()
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun addPeripheralToGroup(group: String, peripheral: String): MethodResult {
        if (!be.peripherals.contains(peripheral)) return MethodResult.of(false, "There is no such peripheral")
        val group = be.peripheralGroups[group] ?: return MethodResult.of(false, "There is no such group")
        if (group.peripherals.contains(peripheral)) {
            return MethodResult.of(false, "Peripheral already in the group")
        }
        group.peripherals.add(peripheral)
        be.pushData()
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun removePeripheralToGroup(group: String, peripheral: String): MethodResult {
        if (!be.peripherals.contains(peripheral)) return MethodResult.of(false, "There is no such peripheral")
        val group = be.peripheralGroups[group] ?: return MethodResult.of(false, "There is no such group")
        if (!group.peripherals.contains(peripheral)) {
            return MethodResult.of(false, "Peripheral not in the group")
        }
        group.peripherals.remove(peripheral)
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
    fun get(group: String): MethodResult {
        val group = be.peripheralGroups[group] ?: return MethodResult.of()
        return MethodResult.of(
            *group.peripherals.filter { be.peripherals.contains(it) }.toTypedArray(),
        )
    }
}
