package site.siredvin.peripheralworks.integrations.emi

import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry

class Entrypoint: EmiPlugin {
    override fun register(p0: EmiRegistry) {
        CommonEntrypoint.register(p0)
    }

}