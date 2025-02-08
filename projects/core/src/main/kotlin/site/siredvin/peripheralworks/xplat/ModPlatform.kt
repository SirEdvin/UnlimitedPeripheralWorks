package site.siredvin.peripheralworks.xplat

import site.siredvin.peripheralium.storages.energy.EnergyUnit
import site.siredvin.peripheralium.xplat.BasePlatform
import site.siredvin.peripheralium.xplat.ModInformationTracker

object ModPlatform : BasePlatform {
    private var impl: ModInnerPlatform? = null
    private val innerModInformationTracker = ModInformationTracker()

    fun configure(impl: ModInnerPlatform) {
        this.impl = impl
    }

    override val baseInnerPlatform: ModInnerPlatform
        get() {
            if (impl == null) {
                throw IllegalStateException("You should configure upw ModPlatform first")
            }
            return impl!!
        }

    override val modInformationTracker: ModInformationTracker
        get() = innerModInformationTracker

    val commonEnergy: EnergyUnit
        get() = baseInnerPlatform.commonEnergy
}
