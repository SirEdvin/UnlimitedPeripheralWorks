package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEFluidKey
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.AEKey
import appeng.api.stacks.GenericStack
import appeng.api.storage.AEKeyFilter
import appeng.blockentity.grid.AENetworkBlockEntity
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.material.Fluids
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.common.ExtractorProxy
import site.siredvin.peripheralium.common.configuration.PeripheraliumConfig
import site.siredvin.peripheralium.extra.plugins.FluidStoragePlugin
import site.siredvin.peripheralium.extra.plugins.ItemStoragePlugin
import site.siredvin.peripheralium.util.representation.LuaRepresentation
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import java.util.*
import java.util.function.Predicate
import kotlin.math.min

class MENetworkBlockPlugin(private val level: Level, private val entity: AENetworkBlockEntity): IPeripheralPlugin {

    /*
    Still todo:
        - get information about crafting CPUs
        - get information about available patterns
        - schedule crafting task
        - information about current crafting tasks
        - ability to disable crafting tasks
     */

    companion object {
        const val PLUGIN_TYPE = "ae2"
    }

    class Provider: PeripheralPluginProvider {
        override val pluginType: String
            get() = PLUGIN_TYPE

        override val conflictWith: Set<String>
            get() = setOf(ItemStoragePlugin.PLUGIN_TYPE, FluidStoragePlugin.PLUGIN_TYPE)

        override val priority: Int
            get() = 1000

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!Configuration.enableMEInterface)
                return null
            val entity = level.getBlockEntity(pos)
            if (entity !is AENetworkBlockEntity)
                return null
            return MENetworkBlockPlugin(level, entity)
        }
    }

    fun genericStackToMap(stack: GenericStack): MutableMap<String, Any> {
        if (stack.what is AEItemKey) {
            val base = LuaRepresentation.forItemStack((stack.what as AEItemKey).toStack(stack.amount.toInt()))
            base["type"] = "item"
            return base
        }
        val base = mutableMapOf<String, Any>()
        base["type"] = "fluid"
        base["name"] = Registry.FLUID.getKey((stack.what as AEFluidKey).fluid).toString()
        base["count"] = stack.amount.toInt() / FluidStoragePlugin.FORGE_COMPACT_DEVIDER
        return base
    }

    @LuaFunction(mainThread = true)
    fun items(): MethodResult {
        val inventory = entity.mainNode.grid?.storageService?.inventory ?: throw LuaException("Not correctly configured AE2 Network")
        val items = mutableListOf<Map<String, Any>>()
        inventory.availableStacks.forEach {
            val aeKey = it.key
            if (aeKey is AEItemKey) {
                val data = LuaRepresentation.forItemStack(aeKey.toStack(it.longValue.toInt()))
                data.remove("maxStackSize")
                items.add(data)
            }
        }
        return MethodResult.of(items)
    }

    @LuaFunction(mainThread = true)
    fun pushItem(computer: IComputerAccess, toName: String, itemName: Optional<String>, limit: Optional<Long>): Long {
        val location: IPeripheral = computer.getAvailablePeripheral(toName)
            ?: throw LuaException("Target '$toName' does not exist")

        val toStorage = ExtractorProxy.extractItemStorage(level, location.target)
            ?: throw LuaException("Target '$toName' is not an fluid inventory")

        val predicate: Predicate<AEItemKey> = if (itemName.isEmpty) {
            Predicate { true }
        } else {
            val item = Registry.ITEM.get(ResourceLocation(itemName.get()))
            if (item == Items.AIR)
                throw LuaException("There is no item ${itemName.get()}")
            Predicate { it.item == item }
        }
        val inventory = entity.mainNode.grid?.storageService?.inventory ?: throw LuaException("Not correctly configured AE2 Network")
        // search for item to push
        val itemToTransfer = inventory.availableStacks.find {
            val aeKey = it.key
            if (aeKey !is AEItemKey)
                return@find false
            return@find predicate.test(aeKey)
        } ?: return 0
        if (itemToTransfer.key !is AEItemKey)
            return 0
        val realLimit = min(min(PeripheraliumConfig.itemStorageTransferLimit, limit.orElse(Long.MAX_VALUE)), itemToTransfer.longValue)
        val extractedLimit = inventory.extract(itemToTransfer.key, realLimit, Actionable.SIMULATE, IActionSource.ofMachine(entity))
        val transferStack = (itemToTransfer.key as AEItemKey).toStack(extractedLimit.toInt())
        val transaction = Transaction.openOuter()
        val insertedAmount = toStorage.insert(ItemVariant.of(transferStack), extractedLimit, transaction)
        if (insertedAmount == 0L) {
            transaction.abort()
            return 0
        }
        val extractedAmount = inventory.extract(itemToTransfer.key, realLimit, Actionable.MODULATE, IActionSource.ofMachine(entity))
        if (extractedAmount != insertedAmount) {
            inventory.insert(itemToTransfer.key, realLimit, Actionable.MODULATE, IActionSource.ofMachine(entity))
            transaction.abort()
            return 0
        }
        transaction.commit()
        return extractedAmount
    }

    @LuaFunction(mainThread = true)
    fun pullItem(computer: IComputerAccess, fromName: String, itemName: Optional<String>, limit: Optional<Long>): Long {
        val location: IPeripheral = computer.getAvailablePeripheral(fromName)
            ?: throw LuaException("Target '$fromName' does not exist")

        val fromStorage = ExtractorProxy.extractItemStorage(level, location.target)
            ?: throw LuaException("Target '$fromName' is not an fluid inventory")

        val predicate: Predicate<ItemVariant> = if (itemName.isEmpty) {
            Predicate { true }
        } else {
            val item = Registry.ITEM.get(ResourceLocation(itemName.get()))
            if (item == Items.AIR)
                throw LuaException("There is no item ${itemName.get()}")
            Predicate { it.isOf(item) }
        }

        val transaction = Transaction.openOuter()
        val extractableResource = StorageUtil.findExtractableContent(fromStorage, predicate, transaction) ?: return 0
        val inventory = entity.mainNode.grid?.storageService?.inventory ?: throw LuaException("Not correctly configured AE2 Network")
        val aeKey = AEItemKey.of(extractableResource.resource)
        val realLimit = min(min(PeripheraliumConfig.itemStorageTransferLimit, limit.orElse(Long.MAX_VALUE)), extractableResource.amount)
        val insertLimit = inventory.insert(aeKey, realLimit, Actionable.SIMULATE, IActionSource.ofMachine(entity))
        if (insertLimit == 0L) {
            transaction.abort()
            return 0
        }
        val extractedAmount = fromStorage.extract(extractableResource.resource, realLimit, transaction)
        if (extractedAmount == 0L) {
            transaction.abort()
            return 0
        }
        val insertedAmount = inventory.insert(aeKey, realLimit, Actionable.MODULATE, IActionSource.ofMachine(entity))
        if (extractedAmount != insertedAmount) {
            inventory.extract(aeKey, realLimit, Actionable.MODULATE, IActionSource.ofMachine(entity))
            transaction.abort()
            return 0
        }
        transaction.commit()
        return insertedAmount
    }


    @LuaFunction(mainThread = true)
    fun tanks(): List<Map<String, *>> {
        val inventory = entity.mainNode.grid?.storageService?.inventory ?: throw LuaException("Not correctly configured AE2 Network")
        val items = mutableListOf<Map<String, Any>>()
        inventory.availableStacks.forEach {
            val aeKey = it.key
            if (aeKey is AEFluidKey) {
                items.add(mapOf(
                    "name" to Registry.FLUID.getKey(aeKey.fluid).toString(),
                    "amount" to it.longValue / FluidStoragePlugin.FORGE_COMPACT_DEVIDER,
                ))
            }
        }
        return items
    }

    @LuaFunction(mainThread = true)
    fun pushFluid(computer: IComputerAccess, toName: String, limit: Optional<Long>, fluidName: Optional<String>): Double {
        val location: IPeripheral = computer.getAvailablePeripheral(toName)
            ?: throw LuaException("Target '$toName' does not exist")

        val toStorage = ExtractorProxy.extractFluidStorage(level, location.target)
            ?: throw LuaException("Target '$toName' is not an fluid inventory")

        val predicate: Predicate<AEFluidKey> = if (fluidName.isEmpty) {
            Predicate { true }
        } else {
            val fluid = Registry.FLUID.get(ResourceLocation(fluidName.get()))
            if (fluid == Fluids.EMPTY)
                throw LuaException("There is no fluid ${fluidName.get()}")
            Predicate { it.fluid.isSame(fluid) }
        }
        val inventory = entity.mainNode.grid?.storageService?.inventory ?: throw LuaException("Not correctly configured AE2 Network")
        // search for item to push
        val fluidToTransfer = inventory.availableStacks.find {
            val aeKey = it.key
            if (aeKey !is AEFluidKey)
                return@find false
            return@find predicate.test(aeKey)
        } ?: return 0.0
        if (fluidToTransfer.key !is AEFluidKey)
            return 0.0
        val realLimit = min(min(PeripheraliumConfig.fluidStorageTransferLimit, limit.map {
            (FluidStoragePlugin.FORGE_COMPACT_DEVIDER * it).toLong()
        }.orElse(Long.MAX_VALUE)), fluidToTransfer.longValue)
        val extractedLimit = inventory.extract(fluidToTransfer.key, realLimit, Actionable.SIMULATE, IActionSource.ofMachine(entity))
        val transferVariant = (fluidToTransfer.key as AEFluidKey).toVariant()
        val transaction = Transaction.openOuter()
        val insertedAmount = toStorage.insert(transferVariant, extractedLimit, transaction)
        if (insertedAmount == 0L) {
            transaction.abort()
            return 0.0
        }
        val extractedAmount = inventory.extract(fluidToTransfer.key, realLimit, Actionable.MODULATE, IActionSource.ofMachine(entity))
        if (extractedAmount != insertedAmount) {
            inventory.insert(fluidToTransfer.key, realLimit, Actionable.MODULATE, IActionSource.ofMachine(entity))
            transaction.abort()
            return 0.0
        }
        transaction.commit()
        return extractedAmount / FluidStoragePlugin.FORGE_COMPACT_DEVIDER
    }

    @LuaFunction(mainThread = true)
    fun pullFluid(computer: IComputerAccess, fromName: String, limit: Optional<Long>, fluidName: Optional<String>): Double {
        val location: IPeripheral = computer.getAvailablePeripheral(fromName)
            ?: throw LuaException("Target '$fromName' does not exist")

        val fromStorage = ExtractorProxy.extractFluidStorage(level, location.target)
            ?: throw LuaException("Target '$fromName' is not an fluid inventory")

        val predicate: Predicate<FluidVariant> = if (fluidName.isEmpty) {
            Predicate { true }
        } else {
            val fluid = Registry.FLUID.get(ResourceLocation(fluidName.get()))
            if (fluid == Fluids.EMPTY)
                throw LuaException("There is no fluid ${fluidName.get()}")
            Predicate { it.isOf(fluid) }
        }

        val transaction = Transaction.openOuter()
        val extractableResource = StorageUtil.findExtractableContent(fromStorage, predicate, transaction) ?: return 0.0
        val inventory = entity.mainNode.grid?.storageService?.inventory ?: throw LuaException("Not correctly configured AE2 Network")
        val aeKey = AEFluidKey.of(extractableResource.resource)
        val realLimit = min(min(PeripheraliumConfig.fluidStorageTransferLimit, limit.map {
            (FluidStoragePlugin.FORGE_COMPACT_DEVIDER * it).toLong()
        }.orElse(Long.MAX_VALUE)), extractableResource.amount)
        val insertLimit = inventory.insert(aeKey, realLimit, Actionable.SIMULATE, IActionSource.ofMachine(entity))
        if (insertLimit == 0L) {
            transaction.abort()
            return 0.0
        }
        val extractedAmount = fromStorage.extract(extractableResource.resource, realLimit, transaction)
        if (extractedAmount == 0L) {
            transaction.abort()
            return 0.0
        }
        val insertedAmount = inventory.insert(aeKey, realLimit, Actionable.MODULATE, IActionSource.ofMachine(entity))
        if (extractedAmount != insertedAmount) {
            inventory.extract(aeKey, realLimit, Actionable.MODULATE, IActionSource.ofMachine(entity))
            transaction.abort()
            return 0.0
        }
        transaction.commit()
        return insertedAmount / FluidStoragePlugin.FORGE_COMPACT_DEVIDER
    }

    @LuaFunction(mainThread = true)
    fun getEnergy(): Double {
        val energyService = entity.mainNode.grid?.energyService ?: return 0.0
        return energyService.storedPower
    }

    @LuaFunction(mainThread = true)
    fun getEnergyCapacity(): Double {
        val energyService = entity.mainNode.grid?.energyService ?: return 0.0
        return energyService.maxStoredPower
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
            if (it.name != null)
                cpuInformation["name"] = it.name.toString()
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
            data.add(mapOf(
                "name" to Registry.FLUID.getKey((it as AEFluidKey).fluid).toString(),
            ))
        }
        return data
    }

    @LuaFunction(mainThread = true)
    fun getPatternsFor(mode: String, id_key: String): List<Map<String, *>> {
        val craftingService = entity.mainNode.grid?.craftingService ?: return emptyList()
        val key: AEKey = when (mode) {
            "fluid" -> {
                val fluid = Registry.FLUID.get(ResourceLocation(id_key))
                AEFluidKey.of(fluid)
            }
            "item" -> {
                val item = Registry.ITEM.get(ResourceLocation(id_key))
                AEItemKey.of(item)
            }
            else -> {
                throw LuaException("first argument should be 'fluid' or 'item'")
            }
        }
        val patterns = craftingService.getCraftingFor(key)
        val data = mutableListOf<Map<String, *>>()
        patterns.forEach { pattern ->
            val patternRepresentation = mutableMapOf<String, Any>()
            val outputs = mutableListOf<Map<String, Any>>()
            val inputs = mutableListOf<Map<String, Any>>()
            pattern.outputs.forEach {outputs.add(genericStackToMap(it))}
            pattern.inputs.forEach {
                val inputData: MutableMap<String, Any>
                if (it.possibleInputs.size == 1) {
                    inputData = genericStackToMap(it.possibleInputs[0])
                    inputData["count"] = (inputData["count"] as Int) * it.multiplier
                } else {
                    val inputVariants = mutableListOf<Map<String, Any>>()
                    it.possibleInputs.forEach {pInput ->
                        val pInputResult = genericStackToMap(pInput)
                        pInputResult["count"] = (pInputResult["count"] as Int) * it.multiplier
                        inputVariants.add(pInputResult)
                    }
                    inputData = mutableMapOf(
                        "variants" to inputVariants
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
}