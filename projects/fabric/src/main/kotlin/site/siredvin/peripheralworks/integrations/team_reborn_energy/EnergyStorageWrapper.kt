package site.siredvin.peripheralworks.integrations.team_reborn_energy

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import site.siredvin.peripheralium.storages.energy.EnergyStack
import site.siredvin.peripheralworks.Energies
import team.reborn.energy.api.EnergyStorage
import java.util.function.Predicate

class EnergyStorageWrapper(private val energyStorage: EnergyStorage) : site.siredvin.peripheralium.storages.energy.EnergyStorage {
    override val capacity: Long
        get() = energyStorage.capacity
    override val energy: EnergyStack
        get() = EnergyStack(Energies.TECH_REBORN_ENERGY, energyStorage.amount)

    override fun setChanged() {
    }

    override fun storeEnergy(stack: EnergyStack): EnergyStack {
        if (stack.unit != Energies.TECH_REBORN_ENERGY) return stack
        Transaction.openOuter().use {
            val stored = energyStorage.insert(stack.amount, it)
            it.commit()
            stack.shrink(stored)
            return stack
        }
    }

    override fun takeEnergy(predicate: Predicate<EnergyStack>, limit: Long): EnergyStack {
        if (!predicate.test(energy)) return EnergyStack.EMPTY
        Transaction.openOuter().use {
            val extractedAmount = energyStorage.extract(limit, it)
            it.commit()
            return EnergyStack(Energies.TECH_REBORN_ENERGY, extractedAmount)
        }
    }
}
