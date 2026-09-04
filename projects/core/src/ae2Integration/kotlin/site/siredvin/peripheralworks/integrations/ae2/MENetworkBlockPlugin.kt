package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.stacks.AEFluidKey
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.AEKey
import appeng.blockentity.grid.AENetworkBlockEntity
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.broccolium.modules.platform.PlatformRegistries
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.integration.AE2Configuration
import site.siredvin.peripheralworks.integrations.ae2.AE2Helper.buildKey
import site.siredvin.peripheralworks.integrations.ae2.AE2Helper.genericStackToMap
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation

class MENetworkBlockPlugin(private val entity: AENetworkBlockEntity) : IPeripheralPlugin {
    override val additionalType
        get() = if (entity is MENetworkPeripheralBlockEntity) "ae2_network_access" else null

    companion object {
        const val PLUGIN_TYPE = "ae2"
    }

    object Provider : PeripheralPluginProvider {
        override val pluginType: String
            get() = PLUGIN_TYPE

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!AE2Configuration.enableMEInterface) {
                return null
            }
            val entity = level.getBlockEntity(pos)
            // Preserve existing worlds that wrap arbitrary AE2 network blocks.
            if (entity !is AENetworkBlockEntity) {
                return null
            }
            return MENetworkBlockPlugin(entity)
        }
    }

    private val grid
        get() = entity.mainNode.takeIf { it.isActive }?.grid

    @LuaFunction(mainThread = true)
    fun getAverageEnergyDemand(): Double {
        val energyService = grid?.energyService ?: return 0.0
        return energyService.avgPowerUsage
    }

    @LuaFunction(mainThread = true)
    fun getAverageEnergyIncome(): Double {
        val energyService = grid?.energyService ?: return 0.0
        return energyService.avgPowerInjection
    }

    @LuaFunction(mainThread = true)
    fun getChannelEnergyDemand(): Double {
        val energyService = grid?.energyService ?: return 0.0
        return energyService.channelPowerUsage
    }

    @LuaFunction(mainThread = true)
    fun getChannelInformation(): Map<String, Any> {
        val pathingService = grid?.pathingService ?: return emptyMap()
        return mapOf(
            "maxChannels" to pathingService.channelMode.adHocNetworkChannels,
            "usedChannels" to pathingService.usedChannels,
        )
    }

    @LuaFunction(mainThread = true)
    fun getCraftingCPUs(): List<Map<String, *>> {
        val craftingService = grid?.craftingService ?: return emptyList()
        val data: MutableList<Map<String, *>> = mutableListOf()
        craftingService.cpus.forEach {
            val cpuInformation = mutableMapOf<String, Any>()
            if (it.name != null) {
                cpuInformation["name"] = it.name!!.string
            }
            cpuInformation["capacity"] = 1 + it.coProcessors
            cpuInformation["storage"] = it.availableStorage
            cpuInformation["isBusy"] = it.isBusy
            data.add(cpuInformation)
        }
        return data
    }

    @LuaFunction(mainThread = true)
    fun getCraftableItems(): List<Map<String, *>> {
        val craftingService = grid?.craftingService ?: return emptyList()
        val data: MutableList<Map<String, *>> = mutableListOf()
        craftingService.getCraftables { it is AEItemKey }.forEach {
            data.add(LuaRepresentation.forItem((it as AEItemKey).item))
        }
        return data
    }

    @LuaFunction(mainThread = true)
    fun getCraftableFluids(): List<Map<String, *>> {
        val craftingService = grid?.craftingService ?: return emptyList()
        val data: MutableList<Map<String, *>> = mutableListOf()
        craftingService.getCraftables { it is AEFluidKey }.forEach {
            data.add(
                mapOf(
                    "name" to PlatformRegistries.FLUIDS.getKey((it as AEFluidKey).fluid).toString(),
                ),
            )
        }
        return data
    }

    @LuaFunction(mainThread = true)
    fun getPatternsFor(mode: String, id_key: String): List<Map<String, *>> {
        val craftingService = grid?.craftingService ?: return emptyList()
        val key: AEKey = buildKey(mode, id_key)
        val patterns = craftingService.getCraftingFor(key)
        val data = mutableListOf<Map<String, *>>()
        patterns.forEach { pattern ->
            data.add(AE2Helper.patternToMap(pattern))
        }
        return data
    }

    @LuaFunction(mainThread = true)
    fun getActiveCraftings(): MethodResult {
        val craftingService = grid?.craftingService ?: return MethodResult.of(null, "AE2 network is not connected")
        val craftingList: MutableList<Map<String, Any>> = mutableListOf()
        craftingService.cpus.forEach {
            if (it.isBusy && it.jobStatus != null) {
                val jobStatus = it.jobStatus!!
                val baseMap = mutableMapOf(
                    "target" to genericStackToMap(jobStatus.crafting),
                    "amount" to jobStatus.totalItems,
                    "progress" to jobStatus.progress,
                )
                if (it.name != null) {
                    baseMap["CPU"] = it.name!!.string
                }
                craftingList.add(baseMap)
            }
        }
        return MethodResult.of(craftingList)
    }
}
