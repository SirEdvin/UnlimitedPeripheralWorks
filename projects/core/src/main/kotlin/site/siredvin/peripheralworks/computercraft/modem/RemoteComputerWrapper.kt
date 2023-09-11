package site.siredvin.peripheralworks.computercraft.modem

import dan200.computercraft.api.filesystem.Mount
import dan200.computercraft.api.filesystem.WritableMount
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.peripheral.WorkMonitor
import site.siredvin.peripheralium.api.peripheral.IPeripheralOwner
import javax.annotation.Nonnull

class RemoteComputerWrapper<O : IPeripheralOwner>(
    private val computer: IComputerAccess,
    private val record: PeripheralRecord<O>,
    private val peripheralHubPeripheral: PeripheralHubPeripheral<O>,
) : IComputerAccess {
    override fun mount(desiredLocation: String, mount: Mount): String? {
        return computer.mount(desiredLocation, mount, record.name)
    }

    override fun mount(
        desiredLocation: String,
        mount: Mount,
        driveName: String,
    ): String? {
        return computer.mount(desiredLocation, mount, driveName)
    }

    override fun mountWritable(desiredLocation: String, mount: WritableMount): String? {
        return computer.mountWritable(desiredLocation, mount, record.name)
    }

    override fun mountWritable(
        desiredLocation: String,
        mount: WritableMount,
        driveName: String,
    ): String? {
        return computer.mountWritable(desiredLocation, mount, driveName)
    }

    override fun unmount(location: String?) {
        computer.unmount(location)
    }

    override fun getID(): Int {
        return computer.id
    }

    override fun queueEvent(@Nonnull event: String, vararg arguments: Any) {
        computer.queueEvent(event, *arguments)
    }

    @Nonnull
    override fun getMainThreadMonitor(): WorkMonitor {
        return computer.mainThreadMonitor
    }

    @Nonnull
    override fun getAttachmentName(): String {
        return record.name
    }

    override fun getAvailablePeripherals(): Map<String, IPeripheral> {
        synchronized(peripheralHubPeripheral.peripheralsRecord) {
            return peripheralHubPeripheral.peripheralsRecord.entries.associate {
                it.key to it.value.peripheral
            }
        }
    }

    override fun getAvailablePeripheral(name: String): IPeripheral? {
        synchronized(peripheralHubPeripheral.peripheralsRecord) {
            val record: PeripheralRecord<O> = peripheralHubPeripheral.peripheralsRecord[name] ?: return null
            return record.peripheral
        }
    }
}
