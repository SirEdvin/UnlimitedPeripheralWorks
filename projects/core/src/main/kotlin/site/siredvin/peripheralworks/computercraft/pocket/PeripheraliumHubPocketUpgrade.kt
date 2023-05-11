package site.siredvin.peripheralworks.computercraft.pocket

import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.pocket.IPocketAccess
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralium.computercraft.pocket.BasePocketUpgrade
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.pocket.PocketPeripheraliumHubPeripheral
import java.util.function.Supplier

class PeripheraliumHubPocketUpgrade(private val maxUpdateCount: Supplier<Int>, private val type: String, item: ItemStack): BasePocketUpgrade<PocketPeripheraliumHubPeripheral>(
    ResourceLocation(PeripheralWorksCore.MOD_ID, type), item) {
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
}