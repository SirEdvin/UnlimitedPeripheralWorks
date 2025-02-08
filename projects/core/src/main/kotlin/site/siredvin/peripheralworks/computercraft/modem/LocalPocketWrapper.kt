package site.siredvin.peripheralworks.computercraft.modem

import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import site.siredvin.peripheralworks.computercraft.peripherals.pocket.PocketPeripheraliumHubPeripheral

class LocalPocketWrapper(val access: IPocketAccess, val upgrade: IPocketUpgrade, val id: String, private val origin: PocketPeripheraliumHubPeripheral) : IPocketAccess {

    var peripheral: IPeripheral? = upgrade.createPeripheral(this)

    val upgradeData: UpgradeData<IPocketUpgrade>
        get() = UpgradeData.of(upgrade, upgradeNBTData)

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

    override fun getUpgrade(): UpgradeData<IPocketUpgrade> = upgradeData

    override fun setUpgrade(p0: UpgradeData<IPocketUpgrade>?): Unit = throw IllegalArgumentException("You should not set upgrade for this wrapper")

    override fun getUpgradeNBTData(): CompoundTag = origin.getDataForUpgrade(id)

    override fun updateUpgradeNBTData() {
        access.updateUpgradeNBTData()
    }

    override fun invalidatePeripheral() {
        if (peripheral != null) {
            origin.removeRemotePeripheral(upgrade.upgradeID.toString())
        }
        peripheral = upgrade.createPeripheral(access)
        if (peripheral != null) {
            origin.attachRemotePeripheral(peripheral!!, upgrade.upgradeID.toString())
        }
    }

    @Deprecated("Deprecated in Java")
    override fun getUpgrades(): MutableMap<ResourceLocation, IPeripheral> {
        if (peripheral == null) {
            return mutableMapOf()
        }
        return mutableMapOf(
            upgrade.upgradeID to peripheral!!,
        )
    }
}
