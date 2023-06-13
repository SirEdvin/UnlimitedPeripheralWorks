package site.siredvin.peripheralworks.data

import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleUpgradeDataProvider
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import net.minecraft.data.PackOutput
import net.minecraft.world.item.Item
import site.siredvin.peripheralium.data.blocks.LibTurtleUpgradeDataProvider
import site.siredvin.peripheralium.xplat.XplatRegistries
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.common.setup.TurtleUpgradeSerializers
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform
import java.util.function.Consumer
import java.util.function.Function

class ModTurtleUpgradeDataProvider(output: PackOutput) : LibTurtleUpgradeDataProvider(output, PeripheralWorksPlatform.holder.turtleSerializers) {
    companion object {
        private val REGISTERED_BUILDERS: MutableList<Function<TurtleUpgradeDataProvider, Upgrade<TurtleUpgradeSerialiser<*>>>> =
            mutableListOf()

        fun hookUpgrade(builder: Function<TurtleUpgradeDataProvider, Upgrade<TurtleUpgradeSerialiser<*>>>) {
            REGISTERED_BUILDERS.add(builder)
        }
    }

    override fun registerUpgrades(addUpgrade: Consumer<Upgrade<TurtleUpgradeSerialiser<*>>>) {
        REGISTERED_BUILDERS.forEach {
            it.apply(this).add(addUpgrade)
        }

        addUpgrade.accept(simpleWithCustomItem(TurtleUpgradeSerializers.PERIPHERALIUM_HUB, Items.PERIPHERALIUM_HUB))
        addUpgrade.accept(simpleWithCustomItem(TurtleUpgradeSerializers.NETHERITE_PERIPHERALIUM_HUB, Items.NETHERITE_PERIPHERALIUM_HUB))
        addUpgrade.accept(simpleWithCustomItem(TurtleUpgradeSerializers.UNIVERSAL_SCANNER, Blocks.UNIVERSAL_SCANNER))
        addUpgrade.accept(simpleWithCustomItem(TurtleUpgradeSerializers.ULTIMATE_SENSOR, Blocks.ULTIMATE_SENSOR))
    }
}
