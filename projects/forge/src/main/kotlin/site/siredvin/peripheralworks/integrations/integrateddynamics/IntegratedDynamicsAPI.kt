package site.siredvin.peripheralworks.integrations.integrateddynamics

import com.mojang.brigadier.exceptions.CommandSyntaxException
import dan200.computercraft.api.lua.ILuaAPI
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.shared.util.NBTUtil
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.TagParser
import site.siredvin.peripheralworks.common.configuration.integration.integrateddynamics.Configuration

class IntegratedDynamicsAPI(private val computerID: Int) : ILuaAPI {
    override fun getNames(): Array<String> = arrayOf("integrated_dynamics")

    @LuaFunction
    fun getInput(): MethodResult {
        val inputData = IntegratedDynamicProxy.inputData[computerID] ?: return MethodResult.of(null)
        return MethodResult.of(NBTUtil.toLua(inputData))
    }

    @LuaFunction
    fun getOutput(): MethodResult {
        val inputData = IntegratedDynamicProxy.outputData[computerID] ?: return MethodResult.of(null)
        return MethodResult.of(NBTUtil.toLua(inputData))
    }

    @LuaFunction
    fun setOutput(json: String): MethodResult {
        if (json.length > Configuration.maxJsonSize) {
            return MethodResult.of(null, "JSON size is bigger than allowed")
        }
        val parsedData: CompoundTag
        try {
            parsedData = TagParser.parseTag(json)
        } catch (ex: CommandSyntaxException) {
            return MethodResult.of(null, String.format("Cannot parse json: %s", ex.message))
        }
        IntegratedDynamicProxy.outputData[computerID] = parsedData
        return MethodResult.of(true)
    }
}
