package site.siredvin.peripheralworks.data

import dan200.computercraft.api.pocket.PocketUpgradeDataProvider
import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import net.minecraft.data.PackOutput
import site.siredvin.peripheralium.data.blocks.LibPocketUpgradeDataProvider
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.common.setup.PocketUpgradeSerializers
import site.siredvin.peripheralworks.xplat.ModPlatform
import java.util.function.Consumer
import java.util.function.Function

class ModPocketUpgradeDataProvider(output: PackOutput) : LibPocketUpgradeDataProvider(output, ModPlatform.holder.pocketSerializers) {
    companion object {
        private val REGISTERED_BUILDERS: MutableList<Function<PocketUpgradeDataProvider, Upgrade<PocketUpgradeSerialiser<*>>>> = mutableListOf()

        fun hookUpgrade(builder: Function<PocketUpgradeDataProvider, Upgrade<PocketUpgradeSerialiser<*>>>) {
            REGISTERED_BUILDERS.add(builder)
        }
    }

    override fun registerUpgrades(addUpgrade: Consumer<Upgrade<PocketUpgradeSerialiser<*>>>) {
        REGISTERED_BUILDERS.forEach {
            it.apply(this).add(addUpgrade)
        }
        addUpgrade.accept(simpleWithCustomItem(PocketUpgradeSerializers.PERIPHERALIUM_HUB, Items.PERIPHERALIUM_HUB))
        addUpgrade.accept(simpleWithCustomItem(PocketUpgradeSerializers.NETHERITE_PERIPHERALIUM_HUB, Items.NETHERITE_PERIPHERALIUM_HUB))
        addUpgrade.accept(simpleWithCustomItem(PocketUpgradeSerializers.ULTIMATE_SENSOR, Blocks.ULTIMATE_SENSOR))
        addUpgrade.accept(simpleWithCustomItem(PocketUpgradeSerializers.UNIVERSAL_SCANNER, Blocks.UNIVERSAL_SCANNER))
    }
}
