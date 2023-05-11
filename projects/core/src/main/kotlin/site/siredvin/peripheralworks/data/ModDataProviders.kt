package site.siredvin.peripheralworks.data

import site.siredvin.peripheralium.data.blocks.GeneratorSink

object ModDataProviders {
    fun add(generator: GeneratorSink) {
        generator.add {
            ModRecipeProvider(it)
        }
        generator.add {
            ModPocketUpgradeDataProvider(it)
        }
        generator.add {
            ModTurtleUpgradeDataProvider(it)
        }
    }
}