package site.siredvin.peripheralworks.computercraft.peripherals.pocket

import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.pocket.IPocketUpgrade
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralium.computercraft.peripheral.owner.PocketPeripheralOwner
import site.siredvin.peripheralium.xplat.PeripheraliumPlatform
import site.siredvin.peripheralworks.computercraft.modem.LocalPocketWrapper
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral

class PocketPeripheraliumHubPeripheral(maxUpdateCount: Int, access: IPocketAccess, type: String): PeripheraliumHubPeripheral<PocketPeripheralOwner>(
    maxUpdateCount, PocketPeripheralOwner(access), type
) {

    val activePocketUpgrades: MutableList<LocalPocketWrapper> = mutableListOf()

    init {
        activeUpgrades.forEach {
            val upgrade = PeripheraliumPlatform.getPocketUpgrade(it)
            if (upgrade != null) {
                connectPocketUpgrade(upgrade)
            }
        }
    }

    fun connectPocketUpgrade(upgrade: IPocketUpgrade) {
        val wrapper = LocalPocketWrapper(peripheralOwner.pocket, upgrade, upgrade.upgradeID.toString(), this)
        activePocketUpgrades.add(wrapper)
        if (wrapper.peripheral != null)
            attachRemotePeripheral(wrapper.peripheral!!, upgrade.upgradeID.toString())
    }

    fun disconnectPocketUpgrade(upgrade: IPocketUpgrade) {
        val wrapper = activePocketUpgrades.find { it.upgrade.upgradeID.equals(upgrade.upgradeID) } ?: return
        activePocketUpgrades.remove(wrapper)
        removeRemotePeripheral(upgrade.upgradeID.toString())
    }

    fun attachPocketUpgrade(upgrade: IPocketUpgrade) {
        attachUpgrade(upgrade.upgradeID)
        connectPocketUpgrade (upgrade)
    }

    fun detachPocketUpgrade(upgrade: IPocketUpgrade) {
        detachUpgrade(upgrade.upgradeID)
        disconnectPocketUpgrade(upgrade)
    }

    override fun isUpgradeImpl(stack: ItemStack): Boolean {
        return PeripheraliumPlatform.getPocketUpgrade(stack) != null
    }

    override fun isEquitable(stack: ItemStack): Pair<Boolean?, String?> {
        val upgrade = PeripheraliumPlatform.getPocketUpgrade(stack) ?: return Pair(null, "Item is not an upgrade")
        if (activePocketUpgrades.any { it.upgrade.upgradeID.equals(upgrade.upgradeID)})
            return Pair(null, "Duplicate upgrades are not allowed")
        return Pair(true, null)
    }

    override fun equipImpl(stack: ItemStack): Pair<Boolean?, String?> {
        val upgrade = PeripheraliumPlatform.getPocketUpgrade(stack) ?: return Pair(null, "Item is not an upgrade")
        if (activePocketUpgrades.any { it.upgrade.upgradeID.equals(upgrade.upgradeID)})
            return Pair(null, "Duplicate upgrades are not allowed")
        attachPocketUpgrade(upgrade)
        return Pair(true, null)
    }

    override fun unequipImpl(id: String): ItemStack {
        val upgrade = activePocketUpgrades.find { it.upgrade.upgradeID.toString() == id } ?: return ItemStack.EMPTY
        val craftingStack = upgrade.upgrade.craftingItem
        detachPocketUpgrade(upgrade.upgrade)
        return craftingStack
    }
}