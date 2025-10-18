package site.siredvin.peripheralworks.integrations.fluxnetworks

import dan200.computercraft.api.lua.LuaFunction
import site.siredvin.broccolium.modules.storage.energy.Energies
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import sonar.fluxnetworks.common.connection.FluxNetwork
import sonar.fluxnetworks.common.device.TileFluxController

class FluxControllerPlugin(private val blockEntity: TileFluxController) : IPeripheralPlugin {
    override val additionalType: String
        get() = "flux_controller"

    @LuaFunction(mainThread = true)
    fun getEnergy(): Long = blockEntity.network.statistics.totalEnergy

    @LuaFunction(mainThread = true)
    fun getEnergyCapacity(): Long = blockEntity.network.getLogicalDevices(FluxNetwork.STORAGE).sumOf { it.maxTransferLimit }

    @LuaFunction(mainThread = true)
    fun getEnergyUnit(): String = Energies.FORGE.name

    @LuaFunction(mainThread = true)
    fun getConnections(): Map<String, Any> {
        val connections = blockEntity.network.allConnections
        val result = mutableMapOf<String, Any>()
        connections.forEach {
            result[it.customName] = mapOf(
                "maxTransferLimit" to it.maxTransferLimit,
                "transferBuffer" to it.transferBuffer,
                "transferChange" to it.transferChange,
                "surgeMode" to it.surgeMode,
                "deviceType" to it.deviceType.name,
                "isChunkLoaded" to it.isChunkLoaded
            )
        }
        return result
    }

    @LuaFunction(mainThread = true)
    fun getNetwork(): Map<String, Any> {
        val network = blockEntity.network
        return mapOf(
            "name" to network.networkName,
            "id" to network.networkID,
            "color" to network.networkColor,
            "securityLevel" to network.securityLevel.name
        )
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
