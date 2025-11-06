package site.siredvin.peripheralworks.computercraft.modem

import dan200.computercraft.api.filesystem.Mount
import dan200.computercraft.api.filesystem.WritableMount
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.peripheral.WorkMonitor
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralOwner
import javax.annotation.Nonnull

class RemoteComputerWrapper<O : IPeripheralOwner>(
    private val computer: IComputerAccess,
    private val record: PeripheralRecord<O>,
    private val peripheralHubPeripheral: PeripheralHubPeripheral<O>,
) : IComputerAccess {
    override fun mount(desiredLocation: String, mount: Mount): String? = computer.mount(desiredLocation, mount, record.name)

    override fun mount(
        desiredLocation: String,
        mount: Mount,
        driveName: String,
    ): String? = computer.mount(desiredLocation, mount, driveName)

    override fun mountWritable(desiredLocation: String, mount: WritableMount): String? = computer.mountWritable(desiredLocation, mount, record.name)

    override fun mountWritable(
        desiredLocation: String,
        mount: WritableMount,
        driveName: String,
    ): String? = computer.mountWritable(desiredLocation, mount, driveName)

    override fun unmount(location: String?) {
        computer.unmount(location)
    }

    override fun getID(): Int = computer.id

    override fun queueEvent(event: String, vararg arguments: Any?) {
        computer.queueEvent(event, *arguments)
    }

    @Nonnull
    override fun getMainThreadMonitor(): WorkMonitor = computer.mainThreadMonitor

    @Nonnull
    override fun getAttachmentName(): String = record.name

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
