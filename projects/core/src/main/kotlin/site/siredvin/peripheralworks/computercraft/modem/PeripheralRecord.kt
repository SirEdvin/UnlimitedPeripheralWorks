package site.siredvin.peripheralworks.computercraft.modem

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.core.apis.PeripheralAPI
import dan200.computercraft.core.asm.PeripheralMethod
import site.siredvin.peripheralium.api.peripheral.IPeripheralOwner
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class PeripheralRecord<O: IPeripheralOwner>(val peripheral: IPeripheral, val name: String, val internalID: String, private val modelPeripheral: ModemPeripheral<O>) {
    private val methodMap: Map<String, PeripheralMethod> = PeripheralAPI.getMethods(peripheral)
    private val wrappers: ConcurrentMap<IComputerAccess, RemoteComputerWrapper<O>>

    init {
        wrappers = ConcurrentHashMap()
    }

    val methodNames: Collection<String>
        get() = methodMap.keys

    fun attach(computer: IComputerAccess) {
        peripheral.attach(computer)
        computer.queueEvent("peripheral", name)
        wrappers[computer] = RemoteComputerWrapper(computer, this, modelPeripheral)
    }

    fun detach(computer: IComputerAccess) {
        peripheral.detach(computer)
        computer.queueEvent("peripheral_detach", name)
        wrappers.remove(computer)
    }

    @Throws(LuaException::class)
    fun callMethod(
        access: IComputerAccess,
        context: ILuaContext?,
        methodName: String,
        arguments: IArguments?
    ): MethodResult {
        val method = methodMap[methodName]
            ?: throw LuaException("No such method $methodName")
        return method.apply(peripheral, context, wrappers[access], arguments)
    }
}