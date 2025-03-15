package site.siredvin.peripheralworks.computercraft.peripherals.turtles

import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleSide
import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.core.component.DataComponentType
import net.minecraft.world.item.ItemStack
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.peripheralworks.common.setup.ModDataComponents
import site.siredvin.peripheralworks.computercraft.modem.LocalTurtleWrapper
import site.siredvin.peripheralworks.common.components.PeripheralUpgrades
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IDataStorage
import site.siredvin.tweakium.modules.peripheral.owner.TurtlePeripheralOwner
import site.siredvin.tweakium.modules.platform.ComputerPlatformToolkit
import kotlin.jvm.optionals.getOrNull

class TurtlePeripheraliumHubPeripheral(maxUpdateCount: Int, access: ITurtleAccess, side: TurtleSide, type: String) : PeripheraliumHubPeripheral<TurtlePeripheralOwner, ITurtleUpgrade, LocalTurtleWrapper>(maxUpdateCount, TurtlePeripheralOwner(access, side), type) {

    companion object {
        fun collectUpgradesData(dataStorage: IDataStorage): List<UpgradeData<ITurtleUpgrade>> = dataStorage.patch.get(ModDataComponents.TURTLE_UPGRADES.get())?.getOrNull()?.upgrades ?: emptyList()
    }

    val activeTurtleUpgrades: MutableList<LocalTurtleWrapper> = mutableListOf()
    override val component: DataComponentType<PeripheralUpgrades<ITurtleUpgrade>>
        get() = ModDataComponents.TURTLE_UPGRADES.get()

    override fun getUpgrade(stack: ItemStack): UpgradeData<ITurtleUpgrade>? = ComputerPlatformToolkit.get().getTurtleUpgrade(PlatformToolkit.get().registries!!, stack)
    override fun wrap(owner: TurtlePeripheralOwner, data: UpgradeData<ITurtleUpgrade>): LocalTurtleWrapper = LocalTurtleWrapper(owner.turtle, owner.side, data, this)
}
