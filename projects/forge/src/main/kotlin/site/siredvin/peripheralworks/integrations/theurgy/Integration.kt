package site.siredvin.peripheralworks.integrations.theurgy

import com.klikli_dev.theurgy.registry.CapabilityRegistry
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStorageLookup
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig

class Integration : Runnable {

    override fun run() {
        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)
        if (Configuration.enableMercuryFluxStorage) {
            AgnosticEnergyStorageLookup.addBlockLookup { level, blockPos, blockEntity, direction ->
                if (blockEntity == null) return@addBlockLookup null
                val mfCapability = blockEntity.getCapability(CapabilityRegistry.MERCURY_FLUX)
                if (mfCapability.isPresent) {
                    return@addBlockLookup AgnosticMercuryFluxStorage(mfCapability.resolve().get())
                }
                if (direction != null) {
                    val sidedCap = blockEntity.getCapability(CapabilityRegistry.MERCURY_FLUX, direction)
                    if (sidedCap.isPresent) {
                        return@addBlockLookup AgnosticMercuryFluxStorage(sidedCap.resolve().get())
                    }
                }
                return@addBlockLookup null
            }
        }
    }
}
