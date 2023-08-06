package site.siredvin.peripheralworks.xplat

import site.siredvin.peripheralium.storages.energy.EnergyUnit
import site.siredvin.peripheralium.xplat.BasePlatform
import site.siredvin.peripheralium.xplat.ModInformationTracker

object ModPlatform : BasePlatform {
    private var _IMPL: ModInnerPlatform? = null
    private val _informationTracker = ModInformationTracker()

    fun configure(impl: ModInnerPlatform) {
        _IMPL = impl
    }

    override val baseInnerPlatform: ModInnerPlatform
        get() {
            if (_IMPL == null) {
                throw IllegalStateException("You should configure upw ModPlatform first")
            }
            return _IMPL!!
        }

    override val modInformationTracker: ModInformationTracker
        get() = _informationTracker

    val commonEnergy: EnergyUnit
        get() = baseInnerPlatform.commonEnergy
}
