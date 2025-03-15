package site.siredvin.peripheralworks.computercraft.peripherals.pocket

import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.core.component.DataComponentType
import net.minecraft.world.item.ItemStack
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.peripheralworks.common.setup.ModDataComponents
import site.siredvin.peripheralworks.computercraft.modem.LocalPocketWrapper
import site.siredvin.peripheralworks.common.components.PeripheralUpgrades
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IDataStorage
import site.siredvin.tweakium.modules.peripheral.owner.PocketPeripheralOwner
import site.siredvin.tweakium.modules.platform.ComputerPlatformToolkit
import kotlin.jvm.optionals.getOrNull

class PocketPeripheraliumHubPeripheral(maxUpdateCount: Int, access: IPocketAccess, type: String) :
    PeripheraliumHubPeripheral<PocketPeripheralOwner, IPocketUpgrade, LocalPocketWrapper>(
        maxUpdateCount,
        PocketPeripheralOwner(access),
        type,
    ) {

    companion object {
        fun collectUpgradesData(dataStorage: IDataStorage): List<UpgradeData<IPocketUpgrade>> = dataStorage.patch.get(ModDataComponents.POCKET_UPGRADES.get())?.getOrNull()?.upgrades ?: emptyList()
    }

    override val component: DataComponentType<PeripheralUpgrades<IPocketUpgrade>>
        get() = ModDataComponents.POCKET_UPGRADES.get()

    override fun getUpgrade(stack: ItemStack): UpgradeData<IPocketUpgrade>? = ComputerPlatformToolkit.get().getPocketUpgrade(PlatformToolkit.get().registries!!, stack)
    override fun wrap(owner: PocketPeripheralOwner, data: UpgradeData<IPocketUpgrade>): LocalPocketWrapper = LocalPocketWrapper(owner.pocket, data, this)
}
