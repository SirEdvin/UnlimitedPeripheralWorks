package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.networking.crafting.CalculationStrategy
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEFluidKey
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.AEKey
import appeng.blockentity.grid.AENetworkBlockEntity
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.util.representation.LuaRepresentation
import site.siredvin.peripheralium.xplat.PeripheraliumPlatform
import site.siredvin.peripheralium.xplat.XplatRegistries
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.integrations.ae2.AE2Helper.buildKey
import site.siredvin.peripheralworks.integrations.ae2.AE2Helper.genericStackToMap
import site.siredvin.peripheralworks.integrations.ae2.AE2Helper.keyCounterToLua
import java.util.*
import kotlin.NoSuchElementException

class MENetworkBlockPlugin(private val level: Level, private val entity: AENetworkBlockEntity) : IPeripheralPlugin {
    companion object {
        const val PLUGIN_TYPE = "ae2"
    }

    class Provider : PeripheralPluginProvider {
        override val pluginType: String
            get() = PLUGIN_TYPE

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!Configuration.enableMEInterface) {
                return null
            }
            val entity = level.getBlockEntity(pos)
            if (entity !is AENetworkBlockEntity) {
                return null
            }
            return MENetworkBlockPlugin(level, entity)
        }
    }

    @LuaFunction(mainThread = true)
    fun getAverageEnergyDemand(): Double {
        val energyService = entity.mainNode.grid?.energyService ?: return 0.0
        return energyService.avgPowerUsage
    }

    @LuaFunction(mainThread = true)
    fun getAverageEnergyIncome(): Double {
        val energyService = entity.mainNode.grid?.energyService ?: return 0.0
        return energyService.avgPowerInjection
    }

    @LuaFunction(mainThread = true)
    fun getChannelEnergyDemand(): Double {
        val energyService = entity.mainNode.grid?.energyService ?: return 0.0
        return energyService.channelPowerUsage
    }

    @LuaFunction(mainThread = true)
    fun getChannelInformation(): Map<String, Any> {
        val pathingService = entity.mainNode.grid?.pathingService ?: return emptyMap()
        return mapOf(
            "maxChannels" to pathingService.channelMode.adHocNetworkChannels,
            "usedChannels" to pathingService.usedChannels,
        )
    }

    @LuaFunction(mainThread = true)
    fun getCraftingCPUs(): List<Map<String, *>> {
        val craftingService = entity.mainNode.grid?.craftingService ?: return emptyList()
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
        val craftingService = entity.mainNode.grid?.craftingService ?: return emptyList()
        val data: MutableList<Map<String, *>> = mutableListOf()
        craftingService.getCraftables { it is AEItemKey }.forEach {
            data.add(LuaRepresentation.forItem((it as AEItemKey).item))
        }
        return data
    }

    @LuaFunction(mainThread = true)
    fun getCraftableFluids(): List<Map<String, *>> {
        val craftingService = entity.mainNode.grid?.craftingService ?: return emptyList()
        val data: MutableList<Map<String, *>> = mutableListOf()
        craftingService.getCraftables { it is AEFluidKey }.forEach {
            data.add(
                mapOf(
                    "name" to XplatRegistries.FLUIDS.getKey((it as AEFluidKey).fluid).toString(),
                ),
            )
        }
        return data
    }

    @LuaFunction(mainThread = true)
    fun getPatternsFor(mode: String, id_key: String): List<Map<String, *>> {
        val craftingService = entity.mainNode.grid?.craftingService ?: return emptyList()
        val key: AEKey = buildKey(mode, id_key)
        val patterns = craftingService.getCraftingFor(key)
        val data = mutableListOf<Map<String, *>>()
        patterns.forEach { pattern ->
            val patternRepresentation = mutableMapOf<String, Any>()
            val outputs = mutableListOf<Map<String, Any>>()
            val inputs = mutableListOf<Map<String, Any>>()
            pattern.outputs.forEach { outputs.add(genericStackToMap(it)) }
            pattern.inputs.forEach {
                val inputData: MutableMap<String, Any>
                if (it.possibleInputs.size == 1) {
                    inputData = genericStackToMap(it.possibleInputs[0])
                    inputData["count"] = (inputData["count"] as Int) * it.multiplier
                } else {
                    val inputVariants = mutableListOf<Map<String, Any>>()
                    it.possibleInputs.forEach { pInput ->
                        val pInputResult = genericStackToMap(pInput)
                        pInputResult["count"] = (pInputResult["count"] as Int) * it.multiplier
                        inputVariants.add(pInputResult)
                    }
                    inputData = mutableMapOf(
                        "variants" to inputVariants,
                    )
                }
                inputs.add(inputData)
            }
            patternRepresentation["inputs"] = inputs
            patternRepresentation["outputs"] = outputs
            data.add(patternRepresentation)
        }
        return data
    }

    @LuaFunction(mainThread = true)
    fun getActiveCraftings(): MethodResult {
        val craftingService = entity.mainNode.grid?.craftingService ?: return MethodResult.of(null, "AE2 network is not connected")
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

    @LuaFunction(mainThread = false)
    fun scheduleCrafting(mode: String, id_key: String, amount: Optional<Long>, targetCPU: Optional<String>): MethodResult {
        val craftingService = entity.mainNode.grid?.craftingService ?: return MethodResult.of(null, "AE2 network is not connected")

        val key = buildKey(mode, id_key)
        val source = IActionSource.ofMachine(entity)
        val realAmount = if (mode == "item") {
            amount.orElse(1)
        } else {
            amount.orElse(1000) * PeripheraliumPlatform.fluidCompactDivider
        }.toLong()
        val future = craftingService.beginCraftingCalculation(
            level,
            { source },
            key,
            realAmount,
            CalculationStrategy.REPORT_MISSING_ITEMS,
        )
        val plan = future.get()

        if (!plan.missingItems().isEmpty) {
            return MethodResult.of(false, "Missing items", keyCounterToLua(plan.missingItems()))
        }
        val realTargetCPU = if (targetCPU.isPresent) {
            try {
                craftingService.cpus.first {
                    it.name != null && it.name!!.string.equals(targetCPU.get())
                }
            } catch (e: NoSuchElementException) {
                return MethodResult.of(null, "Cannot find target CPU")
            }
        } else {
            null
        }
        craftingService.submitJob(plan, null, realTargetCPU, false, source)
        return MethodResult.of(true)
    }
}
