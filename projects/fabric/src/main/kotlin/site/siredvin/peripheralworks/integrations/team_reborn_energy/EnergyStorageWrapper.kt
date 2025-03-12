package site.siredvin.peripheralworks.integrations.team_reborn_energy

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStack
import site.siredvin.broccolium.modules.storage.energy.Energies
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorage
import team.reborn.energy.api.EnergyStorage
import java.util.function.Predicate

class EnergyStorageWrapper(private val energyStorage: EnergyStorage) : AgnosticEnergyStorage {
    override val capacity: Long
        get() = energyStorage.capacity
    override val energy: AgnosticEnergyStack
        get() = AgnosticEnergyStack(Energies.REDSTONE_FLUX, energyStorage.amount)

    override fun setChanged() {
    }

    override fun storeEnergy(stack: AgnosticEnergyStack): AgnosticEnergyStack {
        if (stack.unit != Energies.REDSTONE_FLUX) return stack
        Transaction.openOuter().use {
            val stored = energyStorage.insert(stack.amount, it)
            it.commit()
            stack.shrink(stored)
            return stack
        }
    }

    override fun takeEnergy(predicate: Predicate<AgnosticEnergyStack>, limit: Long): AgnosticEnergyStack {
        if (!predicate.test(energy)) return AgnosticEnergyStack.EMPTY
        Transaction.openOuter().use {
            val extractedAmount = energyStorage.extract(limit, it)
            it.commit()
            return AgnosticEnergyStack(Energies.REDSTONE_FLUX, extractedAmount)
        }
    }
}
