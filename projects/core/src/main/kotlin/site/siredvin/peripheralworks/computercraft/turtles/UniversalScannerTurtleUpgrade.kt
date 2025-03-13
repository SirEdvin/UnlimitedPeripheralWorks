package site.siredvin.peripheralworks.computercraft.turtles

import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleSide
import dan200.computercraft.api.upgrades.UpgradeType
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralworks.common.setup.ModTurtleUpgrades
import site.siredvin.peripheralworks.computercraft.peripherals.UniversalScannerPeripheral
import site.siredvin.tweakium.modules.turtle.StatefulPeripheralTurtleUpgrade

class UniversalScannerTurtleUpgrade(stack: ItemStack) : StatefulPeripheralTurtleUpgrade<UniversalScannerPeripheral>(UniversalScannerPeripheral.UPGRADE_ID, stack) {
    override fun buildPeripheral(turtle: ITurtleAccess, side: TurtleSide): UniversalScannerPeripheral = UniversalScannerPeripheral.of(turtle, side)

    override fun getType(): UpgradeType<out ITurtleUpgrade> = ModTurtleUpgrades.UNIVERSAL_SCANNER.get()
}
