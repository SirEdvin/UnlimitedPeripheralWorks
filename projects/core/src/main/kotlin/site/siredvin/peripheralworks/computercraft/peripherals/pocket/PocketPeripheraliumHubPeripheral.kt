package site.siredvin.peripheralworks.computercraft.peripherals.pocket

import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.core.Holder
import net.minecraft.world.item.ItemStack
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.broccolium.modules.platform.api.RegistryWrapper
import site.siredvin.peripheralworks.computercraft.modem.LocalPocketWrapper
import site.siredvin.peripheralworks.computercraft.peripherals.AnyPeripheraliumHubPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IDataStorage
import site.siredvin.tweakium.modules.peripheral.owner.PocketPeripheralOwner
import site.siredvin.tweakium.modules.platform.ComputerPlatformRegistries
import site.siredvin.tweakium.modules.platform.ComputerPlatformToolkit
import kotlin.jvm.optionals.getOrNull

class PocketPeripheraliumHubPeripheral(maxUpdateCount: Int, access: IPocketAccess, type: String) :
    AnyPeripheraliumHubPeripheral<PocketPeripheralOwner, LocalPocketWrapper, IPocketUpgrade>(
        maxUpdateCount,
        PocketPeripheralOwner(access),
        type,
    ) {

    companion object {
        const val POCKET_MODE = "pocket"
        fun collectUpgradesData(dataStorage: IDataStorage): List<UpgradeData<IPocketUpgrade>> {
            return getActiveUpgrades(dataStorage).mapNotNull {
                val upgrade = ComputerPlatformToolkit.get().getPocketUpgrade(it) ?: return@mapNotNull null
                val id = ComputerPlatformRegistries.POCKET_UPGRADES.getKey(upgrade)
                val key = ComputerPlatformRegistries.POCKET_UPGRADES.getResourceKey(upgrade).getOrNull() ?: return@mapNotNull null
                val holder = ComputerPlatformRegistries.POCKET_UPGRADES.get(key).getOrNull() ?: return@mapNotNull null
                return@mapNotNull UpgradeData.of(holder, getDataForUpgradeAsComponent(id.toString(), dataStorage))
            }
        }
    }

    override val activeMode: String
        get() = POCKET_MODE

    override val registry: RegistryWrapper<IPocketUpgrade>
        get() = ComputerPlatformRegistries.POCKET_UPGRADES

    override fun getUpgrade(id: String): IPocketUpgrade? = ComputerPlatformToolkit.get().getPocketUpgrade(id)

    override fun getUpgrade(stack: ItemStack): UpgradeData<IPocketUpgrade>? = ComputerPlatformToolkit.get().getPocketUpgrade(PlatformToolkit.get().registries!!, stack)

    override fun wrap(
        owner: PocketPeripheralOwner,
        holder: Holder.Reference<IPocketUpgrade>,
        id: String,
    ): LocalPocketWrapper = LocalPocketWrapper(
        owner.pocket,
        holder,
        id,
        this,
    )
}
