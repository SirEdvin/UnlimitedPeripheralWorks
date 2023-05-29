package site.siredvin.peripheralworks.data

import dan200.computercraft.api.pocket.PocketUpgradeDataProvider
import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import net.minecraft.data.PackOutput
import java.util.function.Consumer
import java.util.function.Function

class ModPocketUpgradeDataProvider(output: PackOutput) : PocketUpgradeDataProvider(output) {
    companion object {
        private val REGISTERED_BUILDERS: MutableList<Function<PocketUpgradeDataProvider, Upgrade<PocketUpgradeSerialiser<*>>>> = mutableListOf()

        fun hookUpgrade(builder: Function<PocketUpgradeDataProvider, Upgrade<PocketUpgradeSerialiser<*>>>) {
            REGISTERED_BUILDERS.add(builder)
        }
    }

    override fun addUpgrades(addUpgrade: Consumer<Upgrade<PocketUpgradeSerialiser<*>>>?) {
        REGISTERED_BUILDERS.forEach {
            it.apply(this).add(addUpgrade)
        }
    }
}
