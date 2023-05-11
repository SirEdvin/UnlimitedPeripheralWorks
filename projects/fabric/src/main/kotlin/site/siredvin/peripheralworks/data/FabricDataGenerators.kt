package site.siredvin.peripheralworks.data

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import site.siredvin.peripheralium.data.FabricDataGenerators


class FabricDataGenerators: DataGeneratorEntrypoint {
    
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {

        val pack = fabricDataGenerator.createPack()
        ModDataProviders.add(FabricDataGenerators.DataGeneratorWrapper(pack))
    }
}