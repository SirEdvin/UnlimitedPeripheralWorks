package site.siredvin.peripheralworks.computercraft.modem

import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import site.siredvin.peripheralworks.computercraft.peripherals.pocket.PocketPeripheraliumHubPeripheral

class LocalPocketWrapper(private val access: IPocketAccess, override val fullUpgrade: UpgradeData<IPocketUpgrade>, private val origin: PocketPeripheraliumHubPeripheral) :
    IPocketAccess,
    LocalWrapper<IPocketUpgrade> {

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

    override fun getUpgradeData(): DataComponentPatch = fullUpgrade.data

    override fun setUpgradeData(p0: DataComponentPatch?) = origin.setDataForUpdate(id, p0)

    override fun invalidatePeripheral() {
        if (peripheral != null) {
            origin.removeRemotePeripheral(fullUpgrade.holder.key().location().toString())
        }
        if (peripheral != null) {
            origin.attachRemotePeripheral(peripheral, fullUpgrade.holder.key().location().toString())
        }
    }
}
