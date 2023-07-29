package site.siredvin.peripheralworks.computercraft.peripherals.turtles

import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleSide
import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralium.computercraft.peripheral.owner.TurtlePeripheralOwner
import site.siredvin.peripheralium.xplat.PeripheraliumPlatform
import site.siredvin.peripheralworks.computercraft.modem.LocalTurtleWrapper
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral

class TurtlePeripheraliumHubPeripheral(maxUpdateCount: Int, access: ITurtleAccess, side: TurtleSide, type: String) : PeripheraliumHubPeripheral<TurtlePeripheralOwner>(maxUpdateCount, TurtlePeripheralOwner(access, side), type) {

    companion object {
        const val TURTLE_MODE = "turtle"

        fun collectUpgradesData(dataStorage: CompoundTag): List<UpgradeData<ITurtleUpgrade>> {
            return getActiveUpgrades(dataStorage).mapNotNull {
                val upgrade = PeripheraliumPlatform.getTurtleUpgrade(it) ?: return@mapNotNull null
                return@mapNotNull UpgradeData(upgrade, getDataForUpgrade(upgrade.upgradeID.toString(), dataStorage))
            }
        }
    }

    val activeTurtleUpgrades: MutableList<LocalTurtleWrapper> = mutableListOf()

    override val activeMode: String
        get() = TURTLE_MODE

    init {
        activeUpgrades.forEach {
            val upgrade = PeripheraliumPlatform.getTurtleUpgrade(it)
            if (upgrade != null) {
                connectTurtleUpgrade(UpgradeData(upgrade, getDataForUpgrade(upgrade.upgradeID.toString())))
            }
        }
    }

    fun connectTurtleUpgrade(upgrade: UpgradeData<ITurtleUpgrade>) {
        val wrapper = LocalTurtleWrapper(peripheralOwner.turtle, peripheralOwner.side, upgrade.upgrade, upgrade.upgrade.upgradeID.toString(), this)
        if (!upgrade.data.isEmpty) setDataForUpdate(wrapper.id, upgrade.data)
        activeTurtleUpgrades.add(wrapper)
        if (wrapper.peripheral != null) {
            attachRemotePeripheral(wrapper.peripheral, upgrade.upgrade.upgradeID.toString())
        }
    }

    fun disconnectTurtleUpgrade(upgrade: UpgradeData<ITurtleUpgrade>) {
        val wrapper = activeTurtleUpgrades.find { it.upgrade.upgradeID.equals(upgrade.upgrade.upgradeID) } ?: return
        activeTurtleUpgrades.remove(wrapper)
        setDataForUpdate(wrapper.id, null)
        removeRemotePeripheral(upgrade.upgrade.upgradeID.toString())
    }

    fun swapUpgrade(old: UpgradeData<ITurtleUpgrade>?, new: UpgradeData<ITurtleUpgrade>?) {
        if (old != null) {
            detachTurtleUpgrade(old)
        }
        if (new != null) {
            attachTurtleUpgrade(new)
        }
    }

    fun attachTurtleUpgrade(upgrade: UpgradeData<ITurtleUpgrade>) {
        attachUpgrade(upgrade.upgrade.upgradeID)
        connectTurtleUpgrade(upgrade)
    }

    fun detachTurtleUpgrade(upgrade: UpgradeData<ITurtleUpgrade>) {
        detachUpgrade(upgrade.upgrade.upgradeID)
        disconnectTurtleUpgrade(upgrade)
    }

    override fun isUpgradeImpl(stack: ItemStack): Boolean {
        return PeripheraliumPlatform.getTurtleUpgrade(stack) != null
    }

    override fun isEquitable(stack: ItemStack): Pair<Boolean?, String?> {
        val upgrade = PeripheraliumPlatform.getTurtleUpgrade(stack) ?: return Pair(null, "Item is not an upgrade")
        if (activeTurtleUpgrades.any { it.upgrade.upgradeID.equals(upgrade.upgrade.upgradeID) }) {
            return Pair(null, "Duplicate upgrades are not allowed")
        }
        return Pair(true, null)
    }

    override fun equipImpl(stack: ItemStack): Pair<Boolean?, String?> {
        val upgrade = PeripheraliumPlatform.getTurtleUpgrade(stack) ?: return Pair(null, "Item is not an upgrade")
        if (activeTurtleUpgrades.any { it.upgrade.upgradeID.equals(upgrade.upgrade.upgradeID) }) {
            return Pair(null, "Duplicate upgrades are not allowed")
        }
        attachTurtleUpgrade(upgrade)
        return Pair(true, null)
    }

    override fun unequipImpl(id: String): ItemStack {
        val upgrade = activeTurtleUpgrades.find { it.upgrade.upgradeID.toString() == id } ?: return ItemStack.EMPTY
        val upgradeStack = upgrade.upgradeData.upgradeItem
        detachTurtleUpgrade(upgrade.upgradeData)
        return upgradeStack
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TurtlePeripheraliumHubPeripheral) return false
        if (!super.equals(other)) return false

        if (activeTurtleUpgrades != other.activeTurtleUpgrades) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + activeTurtleUpgrades.hashCode()
        return result
    }
}
