package site.siredvin.peripheralworks.integrations.theurgy

import com.klikli_dev.theurgy.content.capability.MercuryFluxStorage
import site.siredvin.broccolium.modules.storage.base.api.SomethingOperator
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStack
import site.siredvin.broccolium.modules.storage.energy.EnergyStorageUtils
import site.siredvin.broccolium.modules.storage.energy.EnergyUnit
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorage
import site.siredvin.peripheralworks.common.setup.ModEnergies
import java.util.function.Predicate

class AgnosticMercuryFluxStorage(private val capability: MercuryFluxStorage) : AgnosticEnergyStorage {
    override val firstEnergy: AgnosticEnergyStack
        get() = AgnosticEnergyStack(unit, capability.energyStored.toLong())
    override val maxStackSize: Long
        get() = capability.maxEnergyStored.toLong()
    override val operator: SomethingOperator<AgnosticEnergyStack, Long>
        get() = EnergyStorageUtils
    override val canExtract: Boolean
        get() = capability.canExtract()

    override fun getContent(): Iterator<AgnosticEnergyStack> = listOf(firstEnergy).iterator()

    override fun take(
        predicate: Predicate<AgnosticEnergyStack>,
        limit: Long,
        simulate: Boolean,
    ): AgnosticEnergyStack {
        if (!predicate.test(firstEnergy)) return AgnosticEnergyStack(unit, 0)
        val extractedEnergy = capability.extractEnergy(limit.toInt(), simulate)
        return AgnosticEnergyStack(unit, extractedEnergy.toLong())
    }

    override val canReceive: Boolean
        get() = capability.canReceive()
    val unit: EnergyUnit
        get() = ModEnergies.MERCURY_FLUX

    override fun store(stack: AgnosticEnergyStack, simulate: Boolean): AgnosticEnergyStack {
        if (!stack.`is`(unit)) return stack
        val storedEnergy = capability.receiveEnergy(stack.amount.toInt(), simulate)
        return AgnosticEnergyStack(unit, stack.amount - storedEnergy)
    }

    override fun setChanged() {
    }
}
