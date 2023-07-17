package site.siredvin.peripheralworks.computercraft.pocket

import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralium.api.pocket.PocketUpgradeHolder
import site.siredvin.peripheralium.computercraft.pocket.StatefulPocketUpgrade
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.pocket.PocketPeripheraliumHubPeripheral
import java.util.function.Supplier

class PeripheraliumHubPocketUpgrade(private val maxUpdateCount: Supplier<Int>, private val type: String, item: ItemStack) :
    StatefulPocketUpgrade<PocketPeripheraliumHubPeripheral>(
        ResourceLocation(PeripheralWorksCore.MOD_ID, type),
        item,
    ),
    PocketUpgradeHolder {
    override fun getPeripheral(access: IPocketAccess): PocketPeripheraliumHubPeripheral {
        return PocketPeripheraliumHubPeripheral(maxUpdateCount.get(), access, type)
    }

    override fun update(access: IPocketAccess, peripheral: IPeripheral?) {
        super.update(access, peripheral)
        if (peripheral is PocketPeripheraliumHubPeripheral) {
            peripheral.activePocketUpgrades.forEach {
                it.upgrade.update(it, it.peripheral)
            }
        }
    }

    override fun isItemSuitable(stack: ItemStack): Boolean {
        val storedData = stack.getTagElement(STORED_DATA_TAG) ?: return super.isItemSuitable(stack)
        val mode = storedData.getString(PeripheraliumHubPeripheral.MODE_TAG)
        if (mode.isNotEmpty() && mode != PocketPeripheraliumHubPeripheral.POCKET_MODE) return false
        return super.isItemSuitable(stack)
    }

    override fun getInternalUpgrades(pocket: IPocketAccess): List<UpgradeData<IPocketUpgrade>> {
        // TODO: wait for api to properly implement
        return emptyList()
    }
}
