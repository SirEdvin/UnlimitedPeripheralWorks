package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.config.Actionable
import appeng.api.config.PowerMultiplier
import appeng.api.networking.energy.IEnergyService
import appeng.blockentity.grid.AENetworkBlockEntity
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStack
import site.siredvin.broccolium.modules.storage.energy.Energies
import site.siredvin.broccolium.modules.storage.energy.EnergyUnit
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorage
import java.util.function.Predicate

class AEEnergyStorage(private val energyService: IEnergyService, private val entity: AENetworkBlockEntity) : AgnosticEnergyStorage {
    override val capacity: Long
        get() = energyService.maxStoredPower.toLong()
    override val canExtract: Boolean
        get() = true
    override val energy: AgnosticEnergyStack
        get() = AgnosticEnergyStack(Energies.REDSTONE_FLUX, energyService.storedPower.toLong())

    override fun setChanged() {
        entity.setChanged()
    }

    override val canReceive: Boolean
        get() = true
    override val unit: EnergyUnit
        get() = Energies.REDSTONE_FLUX

    override fun storeEnergy(stack: AgnosticEnergyStack): AgnosticEnergyStack {
        if (stack.unit != Energies.REDSTONE_FLUX) return stack
        val leftover = energyService.injectPower(stack.amount.toDouble(), Actionable.MODULATE)
        return stack.copyWithCount(leftover.toLong())
    }

    override fun takeEnergy(predicate: Predicate<AgnosticEnergyStack>, limit: Long): AgnosticEnergyStack {
        if (!predicate.test(energy)) return AgnosticEnergyStack(Energies.REDSTONE_FLUX, 0)
        val extracted = energyService.extractAEPower(limit.toDouble(), Actionable.MODULATE, PowerMultiplier.CONFIG)
        return AgnosticEnergyStack(Energies.REDSTONE_FLUX, extracted.toLong())
    }
}
