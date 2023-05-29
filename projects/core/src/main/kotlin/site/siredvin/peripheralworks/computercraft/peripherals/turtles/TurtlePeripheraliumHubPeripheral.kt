package site.siredvin.peripheralworks.computercraft.peripherals.turtles

import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleSide
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralium.computercraft.peripheral.owner.TurtlePeripheralOwner
import site.siredvin.peripheralium.xplat.PeripheraliumPlatform
import site.siredvin.peripheralworks.computercraft.modem.LocalTurtleWrapper
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral

class TurtlePeripheraliumHubPeripheral(maxUpdateCount: Int, access: ITurtleAccess, side: TurtleSide, type: String) : PeripheraliumHubPeripheral<TurtlePeripheralOwner>(maxUpdateCount, TurtlePeripheralOwner(access, side), type) {

    val activeTurtleUpgrades: MutableList<LocalTurtleWrapper> = mutableListOf()

    init {
        activeUpgrades.forEach {
            val upgrade = PeripheraliumPlatform.getTurtleUpgrade(it)
            if (upgrade != null) {
                connectTurtleUpgrade(upgrade)
            }
        }
    }

    fun connectTurtleUpgrade(upgrade: ITurtleUpgrade) {
        val wrapper = LocalTurtleWrapper(peripheralOwner.turtle, peripheralOwner.side, upgrade, upgrade.upgradeID.toString(), this)
        activeTurtleUpgrades.add(wrapper)
        if (wrapper.peripheral != null) {
            attachRemotePeripheral(wrapper.peripheral, upgrade.upgradeID.toString())
        }
    }

    fun disconnectTurtleUpgrade(upgrade: ITurtleUpgrade) {
        val wrapper = activeTurtleUpgrades.find { it.upgrade.upgradeID.equals(upgrade.upgradeID) } ?: return
        activeTurtleUpgrades.remove(wrapper)
        removeRemotePeripheral(upgrade.upgradeID.toString())
    }

    fun swapUpgrade(old: ITurtleUpgrade?, new: ITurtleUpgrade?) {
        if (old != null) {
            detachTurtleUpgrade(old)
        }
        if (new != null) {
            attachTurtleUpgrade(new)
        }
    }

    fun attachTurtleUpgrade(upgrade: ITurtleUpgrade) {
        attachUpgrade(upgrade.upgradeID)
        connectTurtleUpgrade(upgrade)
    }

    fun detachTurtleUpgrade(upgrade: ITurtleUpgrade) {
        detachUpgrade(upgrade.upgradeID)
        disconnectTurtleUpgrade(upgrade)
    }

    override fun isUpgradeImpl(stack: ItemStack): Boolean {
        return PeripheraliumPlatform.getTurtleUpgrade(stack) != null
    }

    override fun isEquitable(stack: ItemStack): Pair<Boolean?, String?> {
        val upgrade = PeripheraliumPlatform.getTurtleUpgrade(stack) ?: return Pair(null, "Item is not an upgrade")
        if (activeTurtleUpgrades.any { it.upgrade.upgradeID.equals(upgrade.upgradeID) }) {
            return Pair(null, "Duplicate upgrades are not allowed")
        }
        return Pair(true, null)
    }

    override fun equipImpl(stack: ItemStack): Pair<Boolean?, String?> {
        val upgrade = PeripheraliumPlatform.getTurtleUpgrade(stack) ?: return Pair(null, "Item is not an upgrade")
        if (activeTurtleUpgrades.any { it.upgrade.upgradeID.equals(upgrade.upgradeID) }) {
            return Pair(null, "Duplicate upgrades are not allowed")
        }
        attachTurtleUpgrade(upgrade)
        return Pair(true, null)
    }

    override fun unequipImpl(id: String): ItemStack {
        val upgrade = activeTurtleUpgrades.find { it.upgrade.upgradeID.toString() == id } ?: return ItemStack.EMPTY
        val craftingStack = upgrade.upgrade.craftingItem
        detachTurtleUpgrade(upgrade.upgrade)
        return craftingStack
    }
}
