package site.siredvin.peripheralworks.computercraft.turtles

import com.google.common.cache.CacheBuilder
import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleSide
import dan200.computercraft.api.upgrades.UpgradeData
import dan200.computercraft.api.upgrades.UpgradeType
import net.minecraft.core.component.DataComponents
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.turtles.TurtlePeripheraliumHubPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IDataStorage
import site.siredvin.tweakium.modules.peripheral.util.DataStorageUtil
import site.siredvin.tweakium.modules.turtle.StatefulPeripheralTurtleUpgrade
import site.siredvin.tweakium.modules.turtle.api.TurtleUpgradeHolder
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

class PeripheraliumHubTurtleUpgrade(private val maxUpdateCount: Supplier<Int>, private val type: String, item: ItemStack) :
    StatefulPeripheralTurtleUpgrade<TurtlePeripheraliumHubPeripheral>(ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, type), item),
    TurtleUpgradeHolder {

    companion object {
        private val internalDataCache = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.SECONDS).build<IDataStorage, List<UpgradeData<ITurtleUpgrade>>>().asMap()
    }

    override fun buildPeripheral(turtle: ITurtleAccess, side: TurtleSide): TurtlePeripheraliumHubPeripheral = TurtlePeripheraliumHubPeripheral(maxUpdateCount.get(), turtle, side, type)

    override fun getType(): UpgradeType<PeripheraliumHubTurtleUpgrade> = UpgradeType.simpleWithCustomItem { stack -> PeripheraliumHubTurtleUpgrade(maxUpdateCount, type, stack) }

    override fun update(turtle: ITurtleAccess, side: TurtleSide) {
        super.update(turtle, side)
        val peripheral = turtle.getPeripheral(side) as? TurtlePeripheraliumHubPeripheral ?: return
        peripheral.activeTurtleUpgrades.forEach {
            it.upgrade.value().update(it, it.tweakedSide)
        }
    }

    override fun isItemSuitable(stack: ItemStack): Boolean {
        val storedData = stack.get(DataComponents.CUSTOM_DATA) ?: return super.isItemSuitable(stack)
        val mode = storedData.copyTag().getString(PeripheraliumHubPeripheral.MODE_TAG)
        if (mode.isNotEmpty() && mode != TurtlePeripheraliumHubPeripheral.TURTLE_MODE) return false
        return super.isItemSuitable(stack)
    }

    override fun getInternalUpgrades(turtle: ITurtleAccess, side: TurtleSide): List<UpgradeData<ITurtleUpgrade>> = internalDataCache.computeIfAbsent(DataStorageUtil.getDataStorage(turtle, side), TurtlePeripheraliumHubPeripheral::collectUpgradesData)
}
