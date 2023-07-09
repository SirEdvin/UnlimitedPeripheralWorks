package site.siredvin.peripheralworks.computercraft.modem

import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import site.siredvin.peripheralworks.computercraft.peripherals.pocket.PocketPeripheraliumHubPeripheral

class LocalPocketWrapper(val access: IPocketAccess, val upgrade: IPocketUpgrade, val id: String, private val origin: PocketPeripheraliumHubPeripheral) : IPocketAccess {

    var peripheral: IPeripheral? = upgrade.createPeripheral(this)

    val upgradeData: UpgradeData<IPocketUpgrade>
        get() = UpgradeData.of(upgrade, upgradeNBTData)

    override fun getEntity(): Entity? {
        return access.entity
    }

    override fun getColour(): Int {
        return access.colour
    }

    override fun setColour(colour: Int) {
        access.colour = colour
    }

    override fun getLight(): Int {
        return access.light
    }

    override fun setLight(colour: Int) {
        access.light = colour
    }

    override fun getUpgradeNBTData(): CompoundTag {
        return origin.getDataForUpgrade(id)
    }

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
