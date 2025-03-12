package site.siredvin.peripheralworks.computercraft.modem

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.core.methods.PeripheralMethod
import dan200.computercraft.shared.computer.core.ServerContext
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralOwner
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class PeripheralRecord<O : IPeripheralOwner>(val peripheral: IPeripheral, val name: String, val internalID: String, private val modelPeripheral: PeripheralHubPeripheral<O>) {
    private val methodMap: Map<String, PeripheralMethod> =
        ServerContext.get(PlatformToolkit.get().minecraftServer!!).peripheralMethods().getSelfMethods(peripheral)
    private val wrappers: ConcurrentMap<IComputerAccess, RemoteComputerWrapper<O>> = ConcurrentHashMap()

    val methodNames: Collection<String>
        get() = methodMap.keys

    fun attach(computer: IComputerAccess) {
        wrappers[computer] = RemoteComputerWrapper(computer, this, modelPeripheral)
        peripheral.attach(wrappers[computer])
        computer.queueEvent("peripheral", name)
    }

    fun detach(computer: IComputerAccess) {
        peripheral.detach(wrappers[computer])
        computer.queueEvent("peripheral_detach", name)
        wrappers.remove(computer)
    }

    @Throws(LuaException::class)
    fun callMethod(
        access: IComputerAccess,
        context: ILuaContext?,
        methodName: String,
        arguments: IArguments?,
    ): MethodResult {
        val method = methodMap[methodName]
            ?: throw LuaException("No such method $methodName")
        return method.apply(peripheral, context, wrappers[access], arguments)
    }
}
