package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.config.Actionable
import appeng.api.config.PowerMultiplier
import appeng.api.networking.energy.IEnergyService
import appeng.blockentity.grid.AENetworkBlockEntity
import site.siredvin.peripheralium.storages.energy.EnergyStack
import site.siredvin.peripheralium.storages.energy.EnergyStorage
import site.siredvin.peripheralworks.Energies
import java.util.function.Predicate

class AEEnergyStorage(private val energyService: IEnergyService, private val entity: AENetworkBlockEntity) : EnergyStorage {
    override val capacity: Long
        get() = energyService.maxStoredPower.toLong()
    override val energy: EnergyStack
        get() = EnergyStack(Energies.TECH_REBORN_ENERGY, energyService.storedPower.toLong())

    override fun setChanged() {
        entity.setChanged()
    }

    override fun storeEnergy(stack: EnergyStack): EnergyStack {
        if (stack.unit != Energies.TECH_REBORN_ENERGY) return stack
        val leftover = energyService.injectPower(stack.amount.toDouble(), Actionable.MODULATE)
        return stack.copyWithCount(leftover.toLong())
    }

    override fun takeEnergy(predicate: Predicate<EnergyStack>, limit: Long): EnergyStack {
        if (!predicate.test(energy)) return EnergyStack.EMPTY
        val extracted = energyService.extractAEPower(limit.toDouble(), Actionable.MODULATE, PowerMultiplier.CONFIG)
        return EnergyStack(Energies.TECH_REBORN_ENERGY, extracted.toLong())
    }
}
