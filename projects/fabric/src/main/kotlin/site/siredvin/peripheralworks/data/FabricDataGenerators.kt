package site.siredvin.peripheralworks.data

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.data.DataProvider


class FabricDataGenerators: DataGeneratorEntrypoint {
    
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()
        pack.addProvider(DataProvider.Factory { ModPocketUpgradeDataProvider(it) })
        pack.addProvider(DataProvider.Factory { ModTurtleUpgradeDataProvider(it) })
    }
}