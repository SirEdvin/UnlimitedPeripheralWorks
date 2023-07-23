package site.siredvin.peripheralworks.fabric

import site.siredvin.peripheralium.fabric.FabricBaseInnerPlatform
import site.siredvin.peripheralworks.PeripheralWorksCore

object FabricModPlatform: FabricBaseInnerPlatform() {
    override val modID: String
        get() = PeripheralWorksCore.MOD_ID
}