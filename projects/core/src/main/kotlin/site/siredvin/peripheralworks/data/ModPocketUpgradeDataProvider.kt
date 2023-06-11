package site.siredvin.peripheralworks.data

import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.pocket.PocketUpgradeDataProvider
import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import net.minecraft.data.PackOutput
import net.minecraft.world.item.Item
import site.siredvin.peripheralium.data.blocks.LibPocketUpgradeDataProvider
import site.siredvin.peripheralium.xplat.XplatRegistries
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.common.setup.PocketUpgradeSerializers
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform
import java.util.function.Consumer
import java.util.function.Function

class ModPocketUpgradeDataProvider(output: PackOutput) : LibPocketUpgradeDataProvider(output, PeripheralWorksPlatform.pocketUpgrades) {
    companion object {
        private val REGISTERED_BUILDERS: MutableList<Function<PocketUpgradeDataProvider, Upgrade<PocketUpgradeSerialiser<*>>>> = mutableListOf()

        fun hookUpgrade(builder: Function<PocketUpgradeDataProvider, Upgrade<PocketUpgradeSerialiser<*>>>) {
            REGISTERED_BUILDERS.add(builder)
        }
    }

    fun <V: IPocketUpgrade> simpleWithCustomItem(serialiser: PocketUpgradeSerialiser<V>, item: Item): Upgrade<PocketUpgradeSerialiser<*>> {
        return simpleWithCustomItem(XplatRegistries.POCKET_SERIALIZERS.getKey(serialiser), serialiser, item)
    }


    override fun registerUpgrades(addUpgrade: Consumer<Upgrade<PocketUpgradeSerialiser<*>>>) {
        REGISTERED_BUILDERS.forEach {
            it.apply(this).add(addUpgrade)
        }
        addUpgrade.accept(simpleWithCustomItem(PocketUpgradeSerializers.PERIPHERALIUM_HUB.get(), Items.PERIPHERALIUM_HUB.get()))
        addUpgrade.accept(simpleWithCustomItem(PocketUpgradeSerializers.NETHERITE_PERIPHERALIUM_HUB.get(), Items.NETHERITE_PERIPHERALIUM_HUB.get()))
        addUpgrade.accept(simpleWithCustomItem(PocketUpgradeSerializers.ULTIMATE_SENSOR.get(), Blocks.ULTIMATE_SENSOR.get().asItem()))
        addUpgrade.accept(simpleWithCustomItem(PocketUpgradeSerializers.UNIVERSAL_SCANNER.get(), Blocks.UNIVERSAL_SCANNER.get().asItem()))
    }
}
