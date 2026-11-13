package site.siredvin.peripheralworks.computercraft.modem

import dan200.computercraft.api.upgrades.UpgradeBase
import dan200.computercraft.api.upgrades.UpgradeHandle
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral

/** Handle for an upgrade nested in a peripheralium hub, whose item and data live in the hub's own storage. */
class LocalUpgradeHandle<U : UpgradeBase>(
    private val wrapper: LocalWrapper<U>,
    private val origin: PeripheraliumHubPeripheral<*, U, *>,
    private val removed: Runnable,
) : UpgradeHandle<U> {
    override fun getUpgrade(): U = wrapper.fullUpgrade.upgrade()

    override fun getItem(): ItemStack = wrapper.fullUpgrade.upgradeItem

    override fun setItem(item: ItemStack) = origin.setUpgradeItem(wrapper.id, item)

    override fun getData(): DataComponentPatch = wrapper.fullUpgrade.data()

    override fun setData(data: DataComponentPatch) = origin.setUpgradeData(wrapper.id, data)

    override fun remove() = removed.run()
}
