package site.siredvin.peripheralworks.computercraft.peripherals.turtles

import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleSide
import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.core.Holder
import net.minecraft.world.item.ItemStack
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.broccolium.modules.platform.api.RegistryWrapper
import site.siredvin.peripheralworks.computercraft.modem.LocalTurtleWrapper
import site.siredvin.peripheralworks.computercraft.peripherals.AnyPeripheraliumHubPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IDataStorage
import site.siredvin.tweakium.modules.peripheral.owner.TurtlePeripheralOwner
import site.siredvin.tweakium.modules.platform.ComputerPlatformRegistries
import site.siredvin.tweakium.modules.platform.ComputerPlatformToolkit
import kotlin.jvm.optionals.getOrNull

class TurtlePeripheraliumHubPeripheral(maxUpdateCount: Int, access: ITurtleAccess, side: TurtleSide, type: String) : AnyPeripheraliumHubPeripheral<TurtlePeripheralOwner, LocalTurtleWrapper, ITurtleUpgrade>(maxUpdateCount, TurtlePeripheralOwner(access, side), type) {

    companion object {
        const val TURTLE_MODE = "turtle"

        fun collectUpgradesData(dataStorage: IDataStorage): List<UpgradeData<ITurtleUpgrade>> {
            return getActiveUpgrades(dataStorage).mapNotNull {
                val upgrade = ComputerPlatformToolkit.get().getTurtleUpgrade(it) ?: return@mapNotNull null
                val id = ComputerPlatformRegistries.TURTLE_UPGRADES.getKey(upgrade)
                val key = ComputerPlatformRegistries.TURTLE_UPGRADES.getResourceKey(upgrade).getOrNull() ?: return@mapNotNull null
                val holder = ComputerPlatformRegistries.TURTLE_UPGRADES.get(key).getOrNull() ?: return@mapNotNull null
                return@mapNotNull UpgradeData.of(holder, getDataForUpgradeAsComponent(id.toString(), dataStorage))
            }
        }
    }

    val activeTurtleUpgrades: MutableList<LocalTurtleWrapper> = mutableListOf()
    override val registry: RegistryWrapper<ITurtleUpgrade>
        get() = ComputerPlatformRegistries.TURTLE_UPGRADES

    override fun getUpgrade(id: String): ITurtleUpgrade? = ComputerPlatformToolkit.get().getTurtleUpgrade(id)

    override fun getUpgrade(stack: ItemStack): UpgradeData<ITurtleUpgrade>? = ComputerPlatformToolkit.get().getTurtleUpgrade(PlatformToolkit.get().registries!!, stack)

    override fun wrap(
        owner: TurtlePeripheralOwner,
        holder: Holder.Reference<ITurtleUpgrade>,
        id: String,
    ): LocalTurtleWrapper = LocalTurtleWrapper(
        owner.turtle,
        owner.side,
        holder,
        id,
        this,
    )

    override val activeMode: String
        get() = TURTLE_MODE
}
