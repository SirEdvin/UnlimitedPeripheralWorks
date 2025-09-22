package site.siredvin.peripheralworks.integrations.theurgy

import com.klikli_dev.theurgy.registry.CapabilityRegistry
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStorageLookup
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorageExtractor
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig

class Integration : Runnable {

    override fun run() {
        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)
        if (Configuration.enableMercuryFluxStorage) {
            AgnosticEnergyStorageLookup.addEnergyStorageExtractor(
                AgnosticEnergyStorageExtractor { level, blockPos, blockEntity ->
                    if (blockEntity == null) return@AgnosticEnergyStorageExtractor null
                    val mfCapability = blockEntity.getCapability(CapabilityRegistry.MERCURY_FLUX)
                    if (mfCapability.isPresent) {
                        return@AgnosticEnergyStorageExtractor AgnosticMercuryFluxStorage(mfCapability.resolve().get())
                    }
                    return@AgnosticEnergyStorageExtractor null
                },
            )
        }
    }
}
