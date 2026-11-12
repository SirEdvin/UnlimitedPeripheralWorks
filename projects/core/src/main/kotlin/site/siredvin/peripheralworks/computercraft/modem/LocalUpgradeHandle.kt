package site.siredvin.peripheralworks.computercraft.modem

import dan200.computercraft.api.upgrades.UpgradeBase
import dan200.computercraft.api.upgrades.UpgradeHandle
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral

/** Handle for an upgrade nested in a peripheralium hub, whose item and data live in the hub's own storage. */
class LocalUpgradeHandle<U : UpgradeBase>(
    private val upgrade: U,
    private val origin: PeripheraliumHubPeripheral<*>,
    private val changed: Runnable,
    private val removed: Runnable,
) : UpgradeHandle<U> {
    override fun getUpgrade(): U = upgrade

    override fun getItem(): ItemStack = origin.getUpgradeData(upgrade).upgradeItem

    override fun setItem(item: ItemStack) {
        check(upgrade.craftingIngredient.test(item) && upgrade.isItemSuitable(item)) { "Item $item is not suitable for ${upgrade.upgradeID}" }
        origin.setItemForUpgrade(upgrade.upgradeID.toString(), item)
    }

    override fun getData(): CompoundTag = origin.getDataForUpgrade(upgrade.upgradeID.toString())

    override fun setDataChanged() = changed.run()

    override fun remove() = removed.run()
}
