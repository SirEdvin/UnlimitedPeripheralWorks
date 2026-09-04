package site.siredvin.peripheralworks.integrations.embers

import com.rekindled.embers.api.capabilities.EmbersCapabilities
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStorageLookup
import site.siredvin.peripheralworks.common.configuration.integration.EmbersConfiguration

class Integration : Runnable {

    override fun run() {
        if (EmbersConfiguration.enableEmberStorage) {
            AgnosticEnergyStorageLookup.addBlockLookup { level, blockPos, blockEntity, direction ->
                if (blockEntity == null) return@addBlockLookup null
                val capability = blockEntity.getCapability(EmbersCapabilities.EMBER_CAPABILITY)
                if (capability.isPresent) {
                    return@addBlockLookup AgnosticEmberStorage(capability.resolve().get())
                }
                if (direction != null) {
                    val sidedCapability = blockEntity.getCapability(EmbersCapabilities.EMBER_CAPABILITY, direction)
                    if (sidedCapability.isPresent) {
                        return@addBlockLookup AgnosticEmberStorage(sidedCapability.resolve().get())
                    }
                }
                return@addBlockLookup null
            }
        }
    }
}
