package site.siredvin.peripheralworks.integrations.theurgy

import com.klikli_dev.theurgy.registry.CapabilityRegistry
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStorageLookup
import site.siredvin.peripheralworks.common.configuration.integration.TheurgyConfiguration

class Integration : Runnable {

    override fun run() {
        if (TheurgyConfiguration.enableMercuryFluxStorage) {
            AgnosticEnergyStorageLookup.addBlockLookup { level, blockPos, blockEntity, direction ->
                if (blockEntity == null) return@addBlockLookup null
                if (direction == null) return@addBlockLookup null
                val mfCapability = level.getCapability(CapabilityRegistry.MERCURY_FLUX_HANDLER, blockPos, direction)
                if (mfCapability != null) {
                    return@addBlockLookup AgnosticMercuryFluxStorage(mfCapability)
                }
                return@addBlockLookup null
            }
        }
    }
}
