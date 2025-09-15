package site.siredvin.peripheralworks.integrations.embers

import com.rekindled.embers.api.capabilities.EmbersCapabilities
import com.rekindled.embers.api.power.IEmberCapability
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class EmberStoragePlugin(private val level: Level, private val capability: IEmberCapability) : IPeripheralPlugin {
    override val additionalType: String
        get() = "ember_storage"

    fun extractEmberFromUnknown(level: Level, obj: Any?): IEmberCapability? {
        if (obj == null) {
            return null
        }
        if (obj is BlockPos) {
            val blockEntity = level.getBlockEntity(obj)
            if (blockEntity == null) {
                return null
            }
            return blockEntity.getCapability(EmbersCapabilities.EMBER_CAPABILITY).resolve().getOrNull()
        }
        if (obj is BlockEntity) {
            return obj.getCapability(EmbersCapabilities.EMBER_CAPABILITY).resolve().getOrNull()
        }
        throw IllegalArgumentException("Cannot extract storage for $obj")
    }

    fun transfer(limit: Optional<Double>, from: IEmberCapability, to: IEmberCapability): Double {
        val actualLimit = limit.orElse(from.ember).coerceAtMost(from.ember).coerceAtMost(to.emberCapacity - to.ember)
        val removedAmount = from.removeAmount(actualLimit, true)
        val addedAmount = to.addAmount(removedAmount, true)
        val reminder = removedAmount - addedAmount
        if (reminder > 0) {
            from.addAmount(reminder, true)
        }
        return addedAmount
    }

    @LuaFunction(mainThread = true)
    fun getEmberAmount(): Double = capability.ember

    @LuaFunction(mainThread = true)
    fun getEmberCapacity(): Double = capability.emberCapacity

    @LuaFunction(mainThread = true)
    fun pushEmber(computer: IComputerAccess, toName: String, limit: Optional<Double>): Double {
        val location: IPeripheral = computer.getAvailablePeripheral(toName)
            ?: throw LuaException("Target '$toName' does not exist")
        val emberCap = extractEmberFromUnknown(level, location.target)
        if (emberCap == null) {
            throw LuaException("Target '$toName' is not ember storage")
        }
        return transfer(limit, capability, emberCap)
    }

    @LuaFunction(mainThread = true)
    fun pullEmber(computer: IComputerAccess, fromName: String, limit: Optional<Double>): Double {
        val location: IPeripheral = computer.getAvailablePeripheral(fromName)
            ?: throw LuaException("Target '$fromName' does not exist")
        val emberCap = extractEmberFromUnknown(level, location.target)
        if (emberCap == null) {
            throw LuaException("Target '$fromName' is not ember storage")
        }
        return transfer(limit, emberCap, capability)
    }
}
