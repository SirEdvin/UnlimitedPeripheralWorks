package site.siredvin.peripheralworks.common.setup

import site.siredvin.broccolium.modules.storage.energy.EnergyRegistry
import site.siredvin.peripheralworks.data.ModEnergiesText

object ModEnergies {
    val MERCURY_FLUX = EnergyRegistry.register("mercury_flux", ModEnergiesText.MERCURY_FLUX.text, true)
    val EMBER = EnergyRegistry.register("ember", ModEnergiesText.EMBER.text, true)
    val SOURCE = EnergyRegistry.register("source", ModEnergiesText.SOURCE.text, true)

    fun doSomething() {
    }
}
