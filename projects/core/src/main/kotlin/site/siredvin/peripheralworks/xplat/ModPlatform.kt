package site.siredvin.peripheralworks.xplat

import site.siredvin.broccolium.modules.storage.energy.EnergyUnit
import site.siredvin.tweakium.modules.platform.ComputerBasePlatform
import site.siredvin.tweakium.modules.platform.ComputerModInformationTracker

object ModPlatform : ComputerBasePlatform() {
    private var impl: ModInnerPlatform? = null
    private val innerModInformationTracker = ComputerModInformationTracker()

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

    override val modInformationTracker: ComputerModInformationTracker
        get() = innerModInformationTracker

    val commonEnergy: EnergyUnit
        get() = baseInnerPlatform.commonEnergy

    val modList: List<String>
        get() = baseInnerPlatform.modList
    fun getModInformation(mod: String): Map<String, Any>? = baseInnerPlatform.getModInformation(mod)
}
