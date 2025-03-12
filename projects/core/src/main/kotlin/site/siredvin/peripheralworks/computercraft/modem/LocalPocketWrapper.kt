package site.siredvin.peripheralworks.computercraft.modem

import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.phys.Vec3
import site.siredvin.peripheralworks.computercraft.peripherals.pocket.PocketPeripheraliumHubPeripheral
import kotlin.jvm.optionals.getOrNull

class LocalPocketWrapper(private val access: IPocketAccess, override val upgrade: Holder.Reference<IPocketUpgrade>, override val id: String, private val origin: PocketPeripheraliumHubPeripheral) :
    IPocketAccess,
    LocalWrapper<IPocketUpgrade> {

    override val peripheral: IPeripheral? = upgrade.value().createPeripheral(this)

    override val fullUpgradeData: UpgradeData<IPocketUpgrade>
        get() = UpgradeData.of(upgrade, upgradeData)

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

    override fun getUpgrade(): UpgradeData<IPocketUpgrade> = fullUpgradeData

    override fun setUpgrade(p0: UpgradeData<IPocketUpgrade>?): Unit = throw IllegalArgumentException("You should not set upgrade for this wrapper")

    override fun getUpgradeData(): DataComponentPatch = DataComponentPatch.builder().set(DataComponents.CUSTOM_DATA, CustomData.of(origin.getDataForUpgrade(id))).build()

    override fun setUpgradeData(p0: DataComponentPatch?) = origin.setDataForUpdate(id, p0?.get(DataComponents.CUSTOM_DATA)?.getOrNull()?.copyTag())

    override fun invalidatePeripheral() {
        if (peripheral != null) {
            origin.removeRemotePeripheral(upgrade.key().toString())
        }
        if (peripheral != null) {
            origin.attachRemotePeripheral(peripheral, upgrade.key().toString())
        }
    }
}
