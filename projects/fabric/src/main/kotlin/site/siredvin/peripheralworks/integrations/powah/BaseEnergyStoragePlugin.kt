package site.siredvin.peripheralworks.integrations.powah

import dan200.computercraft.api.lua.LuaFunction
import owmii.powah.config.IEnergyConfig
import owmii.powah.lib.block.AbstractEnergyBlock
import owmii.powah.lib.block.AbstractEnergyStorage
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

open class BaseEnergyStoragePlugin(private val storage: AbstractEnergyStorage<*, *>) : IPeripheralPlugin {
    @LuaFunction(mainThread = true)
    fun getEnergyTransfer(): Long = ((storage.block as AbstractEnergyBlock<*, *>).config as IEnergyConfig).getTransfer(storage.variant)
}
