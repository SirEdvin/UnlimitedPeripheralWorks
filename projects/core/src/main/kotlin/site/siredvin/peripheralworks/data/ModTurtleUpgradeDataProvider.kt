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

class ModTurtleUpgradeDataProvider(output: PackOutput) : LibTurtleUpgradeDataProvider(output, PeripheralWorksPlatform.turtleUpgrades) {
    companion object {
        private val REGISTERED_BUILDERS: MutableList<Function<TurtleUpgradeDataProvider, Upgrade<TurtleUpgradeSerialiser<*>>>> =
            mutableListOf()

        fun hookUpgrade(builder: Function<TurtleUpgradeDataProvider, Upgrade<TurtleUpgradeSerialiser<*>>>) {
            REGISTERED_BUILDERS.add(builder)
        }
    }

    fun <V: ITurtleUpgrade> simpleWithCustomItem(serialiser: TurtleUpgradeSerialiser<V>, item: Item): Upgrade<TurtleUpgradeSerialiser<*>> {
        return simpleWithCustomItem(XplatRegistries.TURTLE_SERIALIZERS.getKey(serialiser), serialiser, item)
    }

    override fun registerUpgrades(addUpgrade: Consumer<Upgrade<TurtleUpgradeSerialiser<*>>>) {
        REGISTERED_BUILDERS.forEach {
            it.apply(this).add(addUpgrade)
        }

        simpleWithCustomItem(TurtleUpgradeSerializers.PERIPHERALIUM_HUB.get(), Items.PERIPHERALIUM_HUB.get())
        simpleWithCustomItem(TurtleUpgradeSerializers.NETHERITE_PERIPHERALIUM_HUB.get(), Items.NETHERITE_PERIPHERALIUM_HUB.get())
        simpleWithCustomItem(TurtleUpgradeSerializers.UNIVERSAL_SCANNER.get(), Blocks.UNIVERSAL_SCANNER.get().asItem())
        simpleWithCustomItem(TurtleUpgradeSerializers.ULTIMATE_SENSOR.get(), Blocks.ULTIMATE_SENSOR.get().asItem())
    }
}
