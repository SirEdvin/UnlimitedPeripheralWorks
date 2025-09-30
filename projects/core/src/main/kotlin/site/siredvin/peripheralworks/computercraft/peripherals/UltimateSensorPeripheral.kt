package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.TurtleSide
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.blockentity.UltimateSensorBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.operations.UnconditionalFreeOperations
import site.siredvin.peripheralworks.utils.SensorCollection
import site.siredvin.tweakium.modules.peripheral.OwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralOperation
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.owner.PocketPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.owner.TurtlePeripheralOwner
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

        @Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
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
            owner.attachOperations(cooldownThreshold = PeripheralWorksConfig.cooldownTresholdLevel)
            owner.attachFuel()
            return UltimateSensorPeripheral(owner)
        }

        fun of(pocket: IPocketAccess): UltimateSensorPeripheral {
            val owner = PocketPeripheralOwner(pocket)
            owner.attachOperations(cooldownThreshold = PeripheralWorksConfig.cooldownTresholdLevel)
            owner.attachFuel()
            return UltimateSensorPeripheral(owner)
        }

        fun of(blockEntity: UltimateSensorBlockEntity): UltimateSensorPeripheral {
            val owner = BlockEntityPeripheralOwner(blockEntity)
            owner.attachOperations(cooldownThreshold = PeripheralWorksConfig.cooldownTresholdLevel)
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
    fun listAnalyzers(): Set<String> = ANALYZERS.keys

    @LuaFunction(mainThread = true)
    fun listInspectors(): Set<String> = INSPECTORS.keys

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UltimateSensorPeripheral) return false
        if (!super.equals(other)) return false

        if (isEnabled != other.isEnabled) return false
        if (peripheralOwner != other.peripheralOwner) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + isEnabled.hashCode()
        result = 31 * result + peripheralOwner.hashCode()
        return result
    }
}
