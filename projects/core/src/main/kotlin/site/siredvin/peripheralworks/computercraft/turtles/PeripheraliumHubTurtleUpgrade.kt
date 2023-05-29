package site.siredvin.peripheralworks.computercraft.turtles

import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.TurtleSide
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralium.computercraft.turtle.PeripheralTurtleUpgrade
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.computercraft.peripherals.turtles.TurtlePeripheraliumHubPeripheral
import java.util.function.Supplier

class PeripheraliumHubTurtleUpgrade(private val maxUpdateCount: Supplier<Int>, private val type: String, item: ItemStack) :
    PeripheralTurtleUpgrade<TurtlePeripheraliumHubPeripheral>(ResourceLocation(PeripheralWorksCore.MOD_ID, type), item) {
    override fun buildPeripheral(turtle: ITurtleAccess, side: TurtleSide): TurtlePeripheraliumHubPeripheral {
        return TurtlePeripheraliumHubPeripheral(maxUpdateCount.get(), turtle, side, type)
    }

    override fun update(turtle: ITurtleAccess, side: TurtleSide) {
        super.update(turtle, side)
        val peripheral = turtle.getPeripheral(side) as? TurtlePeripheraliumHubPeripheral ?: return
        peripheral.activeTurtleUpgrades.forEach {
            it.upgrade.update(it, it.tweakedSide)
        }
    }
}
