package site.siredvin.peripheralworks.computercraft.pocket

import com.google.common.cache.CacheBuilder
import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.upgrades.UpgradeData
import dan200.computercraft.api.upgrades.UpgradeType
import net.minecraft.core.component.DataComponents
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.pocket.PocketPeripheraliumHubPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IDataStorage
import site.siredvin.tweakium.modules.peripheral.util.DataStorageUtil
import site.siredvin.tweakium.modules.pocket.StatefulPocketUpgrade
import site.siredvin.tweakium.modules.pocket.api.PocketUpgradeHolder
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

class PeripheraliumHubPocketUpgrade(private val maxUpdateCount: Supplier<Int>, private val type: String, item: ItemStack) :
    StatefulPocketUpgrade<PocketPeripheraliumHubPeripheral>(
        ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, type),
        item,
    ),
    PocketUpgradeHolder {

    companion object {
        private val internalDataCache = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.SECONDS).build<IDataStorage, List<UpgradeData<IPocketUpgrade>>>().asMap()
    }
    override fun getPeripheral(access: IPocketAccess): PocketPeripheraliumHubPeripheral = PocketPeripheraliumHubPeripheral(maxUpdateCount.get(), access, type)
    override fun getType(): UpgradeType<out IPocketUpgrade> = UpgradeType.simpleWithCustomItem { stack -> PeripheraliumHubPocketUpgrade(maxUpdateCount, type, stack) }

    override fun update(access: IPocketAccess, peripheral: IPeripheral?) {
        super.update(access, peripheral)
        if (peripheral is PocketPeripheraliumHubPeripheral) {
            peripheral.activeWrappers.forEach {
                it.upgrade.value().update(it, it.peripheral)
            }
        }
    }

    override fun isItemSuitable(stack: ItemStack): Boolean {
        val storedData = stack.get(DataComponents.CUSTOM_DATA)?.copyTag() ?: return super.isItemSuitable(stack)
        val mode = storedData.getString(PeripheraliumHubPeripheral.MODE_TAG)
        if (mode.isNotEmpty() && mode != PocketPeripheraliumHubPeripheral.POCKET_MODE) return false
        return super.isItemSuitable(stack)
    }

    override fun getInternalUpgrades(pocket: IPocketAccess): List<UpgradeData<IPocketUpgrade>> = internalDataCache.computeIfAbsent(DataStorageUtil.getDataStorage(pocket), PocketPeripheraliumHubPeripheral::collectUpgradesData)
}
