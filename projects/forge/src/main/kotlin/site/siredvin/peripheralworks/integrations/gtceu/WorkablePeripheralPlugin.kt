package site.siredvin.peripheralworks.integrations.gtceu

import com.gregtechceu.gtceu.api.capability.IWorkable
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

class WorkablePeripheralPlugin(private val workable: IWorkable) : IPeripheralPlugin {
    companion object {
        const val TYPE = "gtceu:workable"
    }
    override val additionalType: String
        get() = TYPE

    @LuaFunction(mainThread = true)
    fun getProgress(): MethodResult = MethodResult.of(workable.progress)

    @LuaFunction(mainThread = true)
    fun getMaxProgress(): MethodResult = MethodResult.of(workable.maxProgress)

    @LuaFunction(mainThread = true)
    fun isActive(): MethodResult = MethodResult.of(workable.isActive)
}
