package site.siredvin.peripheralworks.integrations.gtceu

import com.gregtechceu.gtceu.api.capability.ICoverable
import com.gregtechceu.gtceu.integration.cctweaked.peripherals.CoverHolderPeripheral
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

class CoverHolderPeripheralPlugin(private val capability: ICoverable) : IPeripheralPlugin {
    companion object {
        const val TYPE = "gtceu:cover_holder"
    }

    override val additionalType: String
        get() = TYPE

    @LuaFunction(mainThread = true)
    fun setBufferedText(side: String, slot: Int, text: String): MethodResult = CoverHolderPeripheral.setBufferedText(capability, side, slot, text)

    @LuaFunction(mainThread = true)
    fun parsePlaceholders(side: String, text: String): MethodResult = CoverHolderPeripheral.parsePlaceholders(capability, side, text)
}
