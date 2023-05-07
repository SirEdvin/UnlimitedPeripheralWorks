package site.siredvin.peripheralworks.data

import dan200.computercraft.api.turtle.TurtleUpgradeDataProvider
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import net.minecraft.data.PackOutput
import java.util.function.Consumer
import java.util.function.Function

class ModTurtleUpgradeDataProvider(output: PackOutput): TurtleUpgradeDataProvider(output) {
    companion object {
        private val REGISTERED_BUILDERS: MutableList<Function<TurtleUpgradeDataProvider, Upgrade<TurtleUpgradeSerialiser<*>>>> = mutableListOf()

        fun hookUpgrade(builder: Function<TurtleUpgradeDataProvider, Upgrade<TurtleUpgradeSerialiser<*>>>) {
            REGISTERED_BUILDERS.add(builder)
        }
    }
    override fun addUpgrades(addUpgrade: Consumer<Upgrade<TurtleUpgradeSerialiser<*>>>) {
        REGISTERED_BUILDERS.forEach {
            it.apply(this).add(addUpgrade)
        }
    }
}