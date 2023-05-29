package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.TurtleSide
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralium.api.peripheral.IPeripheralOperation
import site.siredvin.peripheralium.api.peripheral.IPeripheralOwner
import site.siredvin.peripheralium.computercraft.peripheral.OwnedPeripheral
import site.siredvin.peripheralium.computercraft.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.peripheralium.computercraft.peripheral.owner.PocketPeripheralOwner
import site.siredvin.peripheralium.computercraft.peripheral.owner.TurtlePeripheralOwner
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.blockentity.UltimateSensorBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.operations.UnconditionalFreeOperations
import site.siredvin.peripheralworks.utils.SensorCollection
import java.util.function.Function

class UltimateSensorPeripheral(owner: IPeripheralOwner) : OwnedPeripheral<IPeripheralOwner>(TYPE, owner) {

    internal data class WrappedCall(val func: Function<IPeripheralOwner, MethodResult>, val operation: IPeripheralOperation<Any?>?) {
        fun call(owner: IPeripheralOwner): MethodResult {
            if (operation == null) {
                return func.apply(owner)
            }
            return owner.withOperation(operation, null, { func.apply(owner) })
        }
    }

    companion object {
        const val TYPE = "ultimate_sensor"
        val UPGRADE_ID = ResourceLocation(PeripheralWorksCore.MOD_ID, TYPE)

        private val ANALYZERS: MutableMap<String, WrappedCall> = mutableMapOf()
        private val INSPECTORS: MutableMap<String, WrappedCall> = mutableMapOf()

        fun registerAnalyzer(name: String, analyzer: Function<IPeripheralOwner, MethodResult>, operation: IPeripheralOperation<Any?>? = null) {
            if (ANALYZERS.containsKey(name)) {
                throw IllegalArgumentException("Cannot register duplicate analyzer for name $name")
            }
            ANALYZERS[name] = WrappedCall(analyzer, operation)
        }

        fun registerInspector(name: String, inspector: Function<IPeripheralOwner, MethodResult>, operation: IPeripheralOperation<Any?>? = null) {
            if (INSPECTORS.containsKey(name)) {
                throw IllegalArgumentException("Cannot register duplicate analyzer for name $name")
            }
            INSPECTORS[name] = WrappedCall(inspector, operation)
        }

        init {
            registerAnalyzer("dimension", SensorCollection::analyzeDimensions)

            registerInspector("dimension", SensorCollection::inspectDimension)
            registerInspector("biome", SensorCollection::inspectBiome)
            registerInspector("weather", SensorCollection::inspectWeather)
            registerInspector("orientation", SensorCollection::inspectOrientationAngle)
            registerInspector("time", SensorCollection::inspectTime)
            registerInspector("light", SensorCollection::inspectLight)
            registerInspector("calendar", SensorCollection::inspectCalendar)
            registerInspector("chunk", SensorCollection::inspectChunk, UnconditionalFreeOperations.INSPECT_CHUNK)
        }

        fun of(turtle: ITurtleAccess, side: TurtleSide): UltimateSensorPeripheral {
            val owner = TurtlePeripheralOwner(turtle, side)
            owner.attachOperations(config = PeripheralWorksConfig)
            owner.attachFuel()
            return UltimateSensorPeripheral(owner)
        }

        fun of(pocket: IPocketAccess): UltimateSensorPeripheral {
            val owner = PocketPeripheralOwner(pocket)
            owner.attachOperations(config = PeripheralWorksConfig)
            owner.attachFuel()
            return UltimateSensorPeripheral(owner)
        }

        fun of(blockEntity: UltimateSensorBlockEntity): UltimateSensorPeripheral {
            val owner = BlockEntityPeripheralOwner(blockEntity)
            owner.attachOperations(config = PeripheralWorksConfig)
            return UltimateSensorPeripheral(owner)
        }
    }

    init {
        val baseList = ArrayList(ANALYZERS.mapNotNull { it.value.operation })
        baseList.addAll(INSPECTORS.mapNotNull { it.value.operation })
        if (baseList.isNotEmpty()) {
            this.addOperations(baseList)
        }
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enableUltimateSensor

    @LuaFunction(mainThread = true)
    fun analyze(name: String): MethodResult {
        val analyzer = ANALYZERS[name] ?: throw LuaException("There is no such analyzer")
        return analyzer.call(peripheralOwner)
    }

    @LuaFunction(mainThread = true)
    fun inspect(name: String): MethodResult {
        val inspector = INSPECTORS[name] ?: throw LuaException("There is no such inspector")
        return inspector.call(peripheralOwner)
    }

    @LuaFunction(mainThread = true)
    fun listAnalyzers(): Set<String> {
        return ANALYZERS.keys
    }

    @LuaFunction(mainThread = true)
    fun listInspectors(): Set<String> {
        return INSPECTORS.keys
    }
}
