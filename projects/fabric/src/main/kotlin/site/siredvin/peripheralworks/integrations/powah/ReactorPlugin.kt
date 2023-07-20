package site.siredvin.peripheralworks.integrations.powah

import dan200.computercraft.api.lua.LuaFunction
import owmii.powah.block.reactor.ReactorTile

class ReactorPlugin(private val reactor: ReactorTile) : BaseEnergyStoragePlugin(reactor) {
    @LuaFunction(mainThread = true)
    fun inspect(): Map<String, Any> {
        return mapOf(
            "autoMode" to reactor.isGenModeOn,
            "currentCarbon" to reactor.carbon.ticks,
            "maxCarbon" to reactor.carbon.max,
            "currentRedstone" to reactor.redstone.ticks,
            "maxRedstone" to reactor.redstone.max,
            "currentUranium" to reactor.fuel.ticks,
            "maxUranium" to reactor.fuel.max,
            "uraniumConsumption" to reactor.calcConsumption(),
            "energyProduction" to reactor.calcProduction(),
            "currentSolidCoolant" to reactor.solidCoolant.ticks,
            "maxSolidCoolant" to reactor.solidCoolant.max,
            "coolantTemp" to reactor.solidCoolantTemp,
            "currentTemp" to reactor.temp.ticks,
            "maxTemp" to reactor.temp.ticks,
        )
    }

    @LuaFunction(mainThread = true)
    fun toggleAutoMode() {
        reactor.isGenModeOn = !reactor.isGenModeOn
    }

    @LuaFunction(mainThread = true)
    fun getEnergyGeneration(): Long {
        return reactor.calcProduction().toLong()
    }
}
