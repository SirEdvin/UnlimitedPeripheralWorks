package site.siredvin.peripheralworks.integrations.fluxnetworks

import dan200.computercraft.api.lua.LuaFunction
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.storages.energy.ForgeEnergies
import sonar.fluxnetworks.common.connection.FluxNetwork
import sonar.fluxnetworks.common.device.TileFluxController

class FluxControllerPlugin(private val blockEntity: TileFluxController) : IPeripheralPlugin {
    override val additionalType: String
        get() = "flux_controller"

    @LuaFunction(mainThread = true)
    fun getEnergy(): Long {
        return blockEntity.network.statistics.totalEnergy
    }

    @LuaFunction(mainThread = true)
    fun getEnergyCapacity(): Long {
        return blockEntity.network.getLogicalDevices(FluxNetwork.STORAGE).sumOf { it.maxTransferLimit }
    }

    @LuaFunction(mainThread = true)
    fun getEnergyUnit(): String {
        return ForgeEnergies.FORGE.name
    }

    @LuaFunction(mainThread = true)
    fun getStatistic(): Map<String, Any> {
        val stat = blockEntity.network.statistics
        return mapOf(
            "controllerCount" to stat.fluxControllerCount,
            "pointCount" to stat.fluxPointCount,
            "plugCount" to stat.fluxPlugCount,
            "storageCount" to stat.fluxStorageCount,
            "totalBuffer" to stat.totalBuffer,
            "totalEnergy" to stat.totalEnergy,
            "energyInput" to stat.energyInput,
            "energyOutput" to stat.energyOutput,
            "averageTick" to stat.averageTickMicro,
        )
    }
}
