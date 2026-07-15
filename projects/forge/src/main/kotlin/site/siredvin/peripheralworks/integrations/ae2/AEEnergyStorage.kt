package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.config.Actionable
import appeng.api.config.PowerMultiplier
import appeng.api.networking.energy.IEnergyService
import appeng.blockentity.grid.AENetworkBlockEntity
import site.siredvin.broccolium.modules.storage.base.api.SomethingOperator
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStack
import site.siredvin.broccolium.modules.storage.energy.Energies
import site.siredvin.broccolium.modules.storage.energy.EnergyStorageUtils
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorage
import java.util.function.Predicate

class AEEnergyStorage(private val energyService: IEnergyService, private val entity: AENetworkBlockEntity) : AgnosticEnergyStorage {
    override val maxStackSize: Long
        get() = energyService.maxStoredPower.toLong()
    override val operator: SomethingOperator<AgnosticEnergyStack, Long>
        get() = EnergyStorageUtils
    override val canExtract: Boolean
        get() = true
    override val firstEnergy: AgnosticEnergyStack
        get() = AgnosticEnergyStack(Energies.REDSTONE_FLUX, energyService.storedPower.toLong())

    override fun getContent(): Iterator<AgnosticEnergyStack> = listOf(firstEnergy).iterator()

    override fun setChanged() {
        entity.setChanged()
    }

    override val canReceive: Boolean
        get() = true

    override fun store(stack: AgnosticEnergyStack, simulate: Boolean): AgnosticEnergyStack {
        if (stack.unit != Energies.REDSTONE_FLUX) return stack
        val leftover = energyService.injectPower(stack.amount.toDouble(), if (simulate) Actionable.SIMULATE else Actionable.MODULATE)
        return stack.copyWithCount(leftover.toLong())
    }

    override fun take(predicate: Predicate<AgnosticEnergyStack>, limit: Long, simulate: Boolean): AgnosticEnergyStack {
        if (!predicate.test(firstEnergy)) return AgnosticEnergyStack(Energies.REDSTONE_FLUX, 0)
        val extracted = energyService.extractAEPower(limit.toDouble(), if (simulate) Actionable.SIMULATE else Actionable.MODULATE, PowerMultiplier.CONFIG)
        return AgnosticEnergyStack(Energies.REDSTONE_FLUX, extracted.toLong())
    }
}
