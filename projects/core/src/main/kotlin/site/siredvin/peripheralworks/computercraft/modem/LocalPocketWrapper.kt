package site.siredvin.peripheralworks.computercraft.modem

import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.upgrades.UpgradeData
import dan200.computercraft.api.upgrades.UpgradeHandle
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import site.siredvin.peripheralworks.computercraft.peripherals.pocket.PocketPeripheraliumHubPeripheral

// The hub keeps nested upgrades readable through CC's deprecated item-less data API, like Tweakium's upgrade bases.
@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class LocalPocketWrapper(private val access: IPocketAccess, private val initial: UpgradeData<IPocketUpgrade>, private val origin: PocketPeripheraliumHubPeripheral) :
    IPocketAccess,
    LocalWrapper<IPocketUpgrade> {

    override val id: String = initial.holder().key().location().toString()

    // The hub's stored copy changes under the wrapper (data and item writes); always read the current one.
    override val fullUpgrade: UpgradeData<IPocketUpgrade>
        get() = origin.storedUpgrade(id) ?: initial

    // Declared before the peripheral, whose construction may already ask for the handle.
    private val handle = LocalUpgradeHandle(this, origin) { setUpgrade(null) }

    override val peripheral: IPeripheral? = fullUpgrade.upgrade().createPeripheral(this)

    override fun getLevel(): ServerLevel = access.level

    override fun getPosition(): Vec3 = access.position

    override fun getEntity(): Entity? = access.entity

    override fun getColour(): Int = access.colour

    override fun setColour(colour: Int) {
        access.colour = colour
    }

    override fun getLight(): Int = access.light

    override fun setLight(colour: Int) {
        access.light = colour
    }

    override fun getUpgrade(): UpgradeData<IPocketUpgrade> = fullUpgrade

    override fun setUpgrade(p0: UpgradeData<IPocketUpgrade>?): Unit = throw IllegalArgumentException("You should not set upgrade for this wrapper")

    override fun getUpgradeHandle(): UpgradeHandle<IPocketUpgrade> = handle

    override fun getUpgradeData(): DataComponentPatch = fullUpgrade.data()

    override fun setUpgradeData(p0: DataComponentPatch) = origin.setDataForUpdate(id, p0)

    override fun invalidatePeripheral() {
        if (peripheral != null) {
            origin.removeRemotePeripheral(fullUpgrade.holder().key().location().toString())
        }
        if (peripheral != null) {
            origin.attachRemotePeripheral(peripheral, fullUpgrade.holder().key().location().toString())
        }
    }
}
