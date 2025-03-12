package site.siredvin.peripheralworks.computercraft.peripherals.pocket

import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralworks.computercraft.modem.LocalPocketWrapper
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IDataStorage
import site.siredvin.tweakium.modules.peripheral.owner.PocketPeripheralOwner
import site.siredvin.tweakium.modules.platform.ComputerPlatformToolkit

class PocketPeripheraliumHubPeripheral(maxUpdateCount: Int, access: IPocketAccess, type: String) :
    PeripheraliumHubPeripheral<PocketPeripheralOwner>(
        maxUpdateCount,
        PocketPeripheralOwner(access),
        type,
    ) {

    companion object {
        const val POCKET_MODE = "pocket"
        fun collectUpgradesData(dataStorage: IDataStorage): List<UpgradeData<IPocketUpgrade>> {
            return getActiveUpgrades(dataStorage).mapNotNull {
                val upgrade = ComputerPlatformToolkit.get().getPocketUpgrade(it) ?: return@mapNotNull null
                return@mapNotNull UpgradeData(upgrade, getDataForUpgrade(upgrade.upgradeID.toString(), dataStorage))
            }
        }
    }

    val activePocketUpgrades: MutableList<LocalPocketWrapper> = mutableListOf()

    override val activeMode: String
        get() = POCKET_MODE

    init {
        activeUpgrades.forEach {
            val upgrade = ComputerPlatformToolkit.get().getPocketUpgrade(it)
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

    override fun isUpgradeImpl(stack: ItemStack): Boolean = ComputerPlatformToolkit.get().getPocketUpgrade(stack) != null

    override fun isEquitable(stack: ItemStack): Pair<Boolean?, String?> {
        val upgrade = ComputerPlatformToolkit.get().getPocketUpgrade(stack) ?: return Pair(null, "Item is not an upgrade")
        if (activePocketUpgrades.any { it.upgrade.upgradeID.equals(upgrade.upgrade.upgradeID) }) {
            return Pair(null, "Duplicate upgrades are not allowed")
        }
        return Pair(true, null)
    }

    override fun equipImpl(stack: ItemStack): Pair<Boolean?, String?> {
        val upgrade = ComputerPlatformToolkit.get().getPocketUpgrade(stack) ?: return Pair(null, "Item is not an upgrade")
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PocketPeripheraliumHubPeripheral) return false
        if (!super.equals(other)) return false

        if (activePocketUpgrades != other.activePocketUpgrades) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + activePocketUpgrades.hashCode()
        return result
    }
}
