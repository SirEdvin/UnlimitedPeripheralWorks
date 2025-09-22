package site.siredvin.peripheralworks.integrations.embers

import com.rekindled.embers.api.power.IEmberCapability
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStack
import site.siredvin.broccolium.modules.storage.energy.EnergyUnit
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorage
import site.siredvin.peripheralworks.common.setup.ModEnergies
import java.util.function.Predicate

class AgnosticEmberStorage(private val capability: IEmberCapability) : AgnosticEnergyStorage {
    override val energy: AgnosticEnergyStack
        get() = AgnosticEnergyStack(unit, capability.ember.toLong())
    override val capacity: Long
        get() = capability.emberCapacity.toLong()
    override val canExtract: Boolean
        get() = true

    override fun takeEnergy(
        predicate: Predicate<AgnosticEnergyStack>,
        limit: Long,
    ): AgnosticEnergyStack {
        if (!predicate.test(energy)) return AgnosticEnergyStack(unit, 0)
        val actualLimit = limit.toDouble().coerceAtMost(capability.ember)
        capability.ember -= actualLimit
        return AgnosticEnergyStack(unit, actualLimit.toLong())
    }

    override val canReceive: Boolean
        get() = true
    override val unit: EnergyUnit
        get() = ModEnergies.EMBER

    override fun storeEnergy(stack: AgnosticEnergyStack): AgnosticEnergyStack {
        if (!stack.`is`(unit)) return stack
        val actualLimit = stack.amount.toDouble().coerceAtMost(capability.emberCapacity - capability.ember)
        capability.ember += actualLimit
        return AgnosticEnergyStack(unit, stack.amount - actualLimit.toLong())
    }

    override fun setChanged() {
        capability.onContentsChanged()
    }
}
