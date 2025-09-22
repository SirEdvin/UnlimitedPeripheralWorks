package site.siredvin.peripheralworks.integrations.theurgy

import com.klikli_dev.theurgy.content.capability.MercuryFluxStorage
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStack
import site.siredvin.broccolium.modules.storage.energy.EnergyUnit
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorage
import site.siredvin.peripheralworks.common.setup.ModEnergies
import java.util.function.Predicate

class AgnosticMercuryFluxStorage(private val capability: MercuryFluxStorage) : AgnosticEnergyStorage {
    override val energy: AgnosticEnergyStack
        get() = AgnosticEnergyStack(unit, capability.energyStored.toLong())
    override val capacity: Long
        get() = capability.maxEnergyStored.toLong()
    override val canExtract: Boolean
        get() = capability.canExtract()

    override fun takeEnergy(
        predicate: Predicate<AgnosticEnergyStack>,
        limit: Long,
    ): AgnosticEnergyStack {
        if (!predicate.test(energy)) return AgnosticEnergyStack(unit, 0)
        val extractedEnergy = capability.extractEnergy(limit.toInt(), false)
        return AgnosticEnergyStack(unit, extractedEnergy.toLong())
    }

    override val canReceive: Boolean
        get() = capability.canReceive()
    override val unit: EnergyUnit
        get() = ModEnergies.MERCURY_FLUX

    override fun storeEnergy(stack: AgnosticEnergyStack): AgnosticEnergyStack {
        if (!stack.`is`(unit)) return stack
        val storedEnergy = capability.receiveEnergy(stack.amount.toInt(), false)
        return AgnosticEnergyStack(unit, stack.amount - storedEnergy)
    }

    override fun setChanged() {
    }
}
