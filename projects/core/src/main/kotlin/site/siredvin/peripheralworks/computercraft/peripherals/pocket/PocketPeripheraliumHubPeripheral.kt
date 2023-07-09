package site.siredvin.peripheralworks.computercraft.peripherals.pocket

import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralium.computercraft.peripheral.owner.PocketPeripheralOwner
import site.siredvin.peripheralium.xplat.PeripheraliumPlatform
import site.siredvin.peripheralworks.computercraft.modem.LocalPocketWrapper
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral

class PocketPeripheraliumHubPeripheral(maxUpdateCount: Int, access: IPocketAccess, type: String) : PeripheraliumHubPeripheral<PocketPeripheralOwner>(
    maxUpdateCount,
    PocketPeripheralOwner(access),
    type,
) {

    companion object {
        const val POCKET_MODE = "pocket"
    }

    val activePocketUpgrades: MutableList<LocalPocketWrapper> = mutableListOf()

    override val activeMode: String
        get() = POCKET_MODE

    init {
        activeUpgrades.forEach {
            val upgrade = PeripheraliumPlatform.getPocketUpgrade(it)
            if (upgrade != null) {
                connectPocketUpgrade(UpgradeData.of(upgrade, getDataForUpgrade(upgrade.upgradeID.toString())))
            }
        }
    }

    fun connectPocketUpgrade(upgrade: UpgradeData<IPocketUpgrade>) {
        val wrapper = LocalPocketWrapper(peripheralOwner.pocket, upgrade.upgrade, upgrade.upgrade.upgradeID.toString(), this)
        if (!upgrade.data.isEmpty) setDataForUpdate(wrapper.id, upgrade.data)
        activePocketUpgrades.add(wrapper)
        if (wrapper.peripheral != null) {
            attachRemotePeripheral(wrapper.peripheral!!, upgrade.upgrade.upgradeID.toString())
        }
    }

    fun disconnectPocketUpgrade(upgrade: UpgradeData<IPocketUpgrade>) {
        val wrapper = activePocketUpgrades.find { it.upgrade.upgradeID.equals(upgrade.upgrade.upgradeID) } ?: return
        activePocketUpgrades.remove(wrapper)
        setDataForUpdate(wrapper.id, null)
        removeRemotePeripheral(upgrade.upgrade.upgradeID.toString())
    }

    fun attachPocketUpgrade(upgrade: UpgradeData<IPocketUpgrade>) {
        attachUpgrade(upgrade.upgrade.upgradeID)
        connectPocketUpgrade(upgrade)
    }

    fun detachPocketUpgrade(upgrade: UpgradeData<IPocketUpgrade>) {
        detachUpgrade(upgrade.upgrade.upgradeID)
        disconnectPocketUpgrade(upgrade)
    }

    override fun isUpgradeImpl(stack: ItemStack): Boolean {
        return PeripheraliumPlatform.getPocketUpgrade(stack) != null
    }

    override fun isEquitable(stack: ItemStack): Pair<Boolean?, String?> {
        val upgrade = PeripheraliumPlatform.getPocketUpgrade(stack) ?: return Pair(null, "Item is not an upgrade")
        if (activePocketUpgrades.any { it.upgrade.upgradeID.equals(upgrade.upgrade.upgradeID) }) {
            return Pair(null, "Duplicate upgrades are not allowed")
        }
        return Pair(true, null)
    }

    override fun equipImpl(stack: ItemStack): Pair<Boolean?, String?> {
        val upgrade = PeripheraliumPlatform.getPocketUpgrade(stack) ?: return Pair(null, "Item is not an upgrade")
        if (activePocketUpgrades.any { it.upgrade.upgradeID.equals(upgrade.upgrade.upgradeID) }) {
            return Pair(null, "Duplicate upgrades are not allowed")
        }
        attachPocketUpgrade(upgrade)
        return Pair(true, null)
    }

    override fun unequipImpl(id: String): ItemStack {
        val upgrade = activePocketUpgrades.find { it.upgrade.upgradeID.toString() == id } ?: return ItemStack.EMPTY
        val upgradeStack = upgrade.upgradeData.upgradeItem
        detachPocketUpgrade(upgrade.upgradeData)
        return upgradeStack
    }
}
