package site.siredvin.peripheralworks.computercraft.modem
import com.google.common.collect.MapMaker
import dan200.computercraft.api.lua.*
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import kotlinx.atomicfu.locks.withLock
import site.siredvin.peripheralium.api.peripheral.IPeripheralOwner
import site.siredvin.peripheralium.computercraft.peripheral.OwnedPeripheral
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.function.Consumer

abstract class ModemPeripheral<O : IPeripheralOwner>(peripheralType: String, owner: O) : OwnedPeripheral<O>(peripheralType, owner) {

    val peripheralsRecord: ConcurrentMap<String, PeripheralRecord<O>> = ConcurrentHashMap()
    private val remotePeripherals =
        Collections.newSetFromMap(MapMaker().concurrencyLevel(4).weakKeys().makeMap<IPeripheral, Boolean>())

    protected open fun selectName(peripheral: IPeripheral): String {
        val maxIndex = peripheralsRecord.keys.stream().filter { key: String ->
            key.startsWith(
                peripheral.type,
            )
        }.map { key: String ->
            val splitName = key.split("_".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            Integer.valueOf(splitName[splitName.size - 1])
        }.max { obj: Int, anotherInteger: Int? -> obj.compareTo(anotherInteger!!) }.orElse(0)
        return peripheral.type + "_" + (maxIndex + 1)
    }

    open fun attachRemotePeripheral(peripheral: IPeripheral, internalID: String) {
        synchronized(peripheralsRecord) {
            connectedComputersLock.withLock {
                if (remotePeripherals.contains(peripheral)) return
                val peripheralName = selectName(peripheral)
                val record = PeripheralRecord(peripheral, peripheralName, internalID, this)
                connectedComputers.forEach { computer: IComputerAccess -> record.attach(computer) }
                remotePeripherals.add(peripheral)
                peripheralsRecord[peripheralName] = record
            }
        }
    }

    open fun removeRemotePeripheral(internalID: String) {
        synchronized(peripheralsRecord) {
            connectedComputersLock.withLock {
                val record = peripheralsRecord.values.find { it.internalID == internalID } ?: return
                connectedComputers.forEach(record::detach)
                remotePeripherals.remove(record.peripheral)
                peripheralsRecord.remove(record.name)
            }
        }
    }

    open fun purgePeripheral() {
        synchronized(peripheralsRecord) {
            connectedComputersLock.withLock {
                peripheralsRecord.values.forEach {
                    connectedComputers.forEach(it::detach)
                    remotePeripherals.remove(it.peripheral)
                    peripheralsRecord.remove(it.name)
                }
            }
        }
    }

    override fun attach(computer: IComputerAccess) {
        super.attach(computer)
        synchronized(peripheralsRecord) {
            peripheralsRecord.values.forEach(
                Consumer { record: PeripheralRecord<O> ->
                    record.attach(
                        computer,
                    )
                },
            )
        }
    }

    override fun detach(computer: IComputerAccess) {
        super.detach(computer)
        synchronized(peripheralsRecord) {
            peripheralsRecord.values.forEach(
                Consumer { record: PeripheralRecord<O> ->
                    record.detach(
                        computer,
                    )
                },
            )
        }
    }

    @Suppress("unused")
    @LuaFunction
    fun getNamesRemote(): Collection<String> {
        return peripheralsRecord.keys
    }

    @Suppress("unused")
    @LuaFunction
    fun isPresentRemote(name: String): Boolean {
        return peripheralsRecord.containsKey(name)
    }

    @Suppress("unused")
    @LuaFunction
    fun isWireless(): Boolean {
        return false
    }

    @Suppress("unused")
    @LuaFunction
    fun getTypeRemote(@Suppress("UNUSED_PARAMETER") computer: IComputerAccess?, name: String): Array<Any>? {
        val record = peripheralsRecord[name] ?: return null
        return arrayOf(record.peripheral.type)
    }

    @LuaFunction
    fun hasTypeRemote(@Suppress("UNUSED_PARAMETER") computer: IComputerAccess, name: String, type: String): Boolean {
        val record = peripheralsRecord[name] ?: return false
        return record.peripheral.type == type || record.peripheral.additionalTypes.contains(type)
    }

    @Suppress("unused")
    @LuaFunction
    fun getMethodsRemote(@Suppress("UNUSED_PARAMETER") computer: IComputerAccess?, name: String): Array<Any>? {
        val record = peripheralsRecord[name] ?: return null
        return arrayOf(record.methodNames)
    }

    @Suppress("unused")
    @LuaFunction
    @Throws(LuaException::class)
    fun callRemote(computer: IComputerAccess, context: ILuaContext?, arguments: IArguments): MethodResult {
        val remoteName = arguments.getString(0)
        val methodName = arguments.getString(1)
        val record = peripheralsRecord[remoteName]
            ?: throw LuaException("No peripheral: $remoteName")
        return record.callMethod(computer, context, methodName, arguments.drop(2))
    }
}
