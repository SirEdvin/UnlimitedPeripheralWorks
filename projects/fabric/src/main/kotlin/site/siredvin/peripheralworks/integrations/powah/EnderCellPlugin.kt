package site.siredvin.peripheralworks.integrations.powah

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import owmii.powah.block.ender.AbstractEnderTile
import site.siredvin.peripheralium.util.assertBetween

class EnderCellPlugin(private val provider: AbstractEnderTile<*>) : BaseEnergyStoragePlugin(provider) {

    @LuaFunction(mainThread = true)
    fun getChannel(): Int {
        return provider.channel.get() + 1
    }

    @LuaFunction(mainThread = true)
    fun setChannel(channel: Int) {
        val realChannel = channel - 1
        assertBetween(channel, provider.channel.min + 1, provider.maxChannels, "channel")
        provider.channel.set(realChannel)
    }

    @LuaFunction(mainThread = true)
    fun getMaxChannel(): Int {
        return provider.maxChannels
    }
}
