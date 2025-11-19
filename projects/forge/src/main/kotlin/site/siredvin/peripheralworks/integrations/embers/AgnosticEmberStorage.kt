package site.siredvin.peripheralworks.integrations.embers

import com.rekindled.embers.api.power.IEmberCapability
import site.siredvin.broccolium.modules.storage.base.api.SomethingOperator
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStack
import site.siredvin.broccolium.modules.storage.energy.EnergyStorageUtils
import site.siredvin.broccolium.modules.storage.energy.EnergyUnit
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorage
import site.siredvin.peripheralworks.common.setup.ModEnergies
import java.util.function.Predicate

class AgnosticEmberStorage(private val capability: IEmberCapability) : AgnosticEnergyStorage {
    override val firstEnergy: AgnosticEnergyStack
        get() = AgnosticEnergyStack(unit, capability.ember.toLong())
    override val maxStackSize: Long
        get() = capability.emberCapacity.toLong()
    override val operator: SomethingOperator<AgnosticEnergyStack, Long>
        get() = EnergyStorageUtils
    override val canExtract: Boolean
        get() = true

    override fun getContent(): Iterator<AgnosticEnergyStack> {
        return listOf(firstEnergy).iterator()
    }

    override fun take(
        predicate: Predicate<AgnosticEnergyStack>,
        limit: Long,
        simulate: Boolean
    ): AgnosticEnergyStack {
        if (!predicate.test(firstEnergy)) return AgnosticEnergyStack(unit, 0)
        val actualLimit = limit.toDouble().coerceAtMost(capability.ember)
        if (!simulate)
            capability.ember -= actualLimit
        return AgnosticEnergyStack(unit, actualLimit.toLong())
    }

    override val canReceive: Boolean
        get() = true
    val unit: EnergyUnit
        get() = ModEnergies.EMBER

    override fun store(stack: AgnosticEnergyStack, simulate: Boolean): AgnosticEnergyStack {
        if (!stack.`is`(unit)) return stack
        val actualLimit = stack.amount.toDouble().coerceAtMost(capability.emberCapacity - capability.ember)
        if (!simulate)
            capability.ember += actualLimit
        return AgnosticEnergyStack(unit, stack.amount - actualLimit.toLong())
    }

    override fun setChanged() {
        capability.onContentsChanged()
    }
}
