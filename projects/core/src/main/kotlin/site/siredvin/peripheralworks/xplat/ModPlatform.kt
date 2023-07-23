package site.siredvin.peripheralworks.xplat

import site.siredvin.peripheralium.xplat.BaseInnerPlatform
import site.siredvin.peripheralium.xplat.BasePlatform
import site.siredvin.peripheralium.xplat.ModInformationTracker

object ModPlatform : BasePlatform {
    private var _IMPL: BaseInnerPlatform? = null
    private val _informationTracker = ModInformationTracker()

    fun configure(impl: BaseInnerPlatform) {
        _IMPL = impl
    }

    override val baseInnerPlatform: BaseInnerPlatform
        get() {
            if (_IMPL == null) {
                throw IllegalStateException("You should configure upw ModPlatform first")
            }
            return _IMPL!!
        }

    override val modInformationTracker: ModInformationTracker
        get() = _informationTracker
}
