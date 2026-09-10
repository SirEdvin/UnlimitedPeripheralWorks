package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.config.Actionable
import appeng.api.config.PowerMultiplier
import appeng.me.helpers.IGridConnectedBlockEntity
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.broccolium.modules.storage.base.api.SomethingOperator
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStack
import site.siredvin.broccolium.modules.storage.energy.Energies
import site.siredvin.broccolium.modules.storage.energy.EnergyStorageUtils
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorage
import java.util.function.Predicate

class AEEnergyStorage(private val entity: IGridConnectedBlockEntity) : AgnosticEnergyStorage {
    private val energyService get() = entity.mainNode.grid?.energyService
    override val maxStackSize: Long
        get() = energyService?.maxStoredPower?.toLong() ?: 0L
    override val operator: SomethingOperator<AgnosticEnergyStack, Long>
        get() = EnergyStorageUtils
    override val canExtract: Boolean
        get() = true
    override val firstEnergy: AgnosticEnergyStack
        get() = AgnosticEnergyStack(Energies.REDSTONE_FLUX, energyService?.storedPower?.toLong() ?: 0L)

    override fun setChanged() {
        entity.saveChanges()
    }

    override val canReceive: Boolean
        get() = true

    override fun store(stack: AgnosticEnergyStack, simulate: Boolean): AgnosticEnergyStack {
        val energyService = energyService ?: return stack
        if (stack.unit != Energies.REDSTONE_FLUX) return stack
        val leftover = energyService.injectPower(stack.amount.toDouble(), if (simulate) Actionable.SIMULATE else Actionable.MODULATE)
        return stack.copyWithCount(leftover.toLong())
    }

    override fun getContent(): Iterator<AgnosticEnergyStack> = listOf(firstEnergy).iterator()

    override fun take(predicate: Predicate<AgnosticEnergyStack>, limit: Long, simulate: Boolean): AgnosticEnergyStack {
        val energyService = energyService ?: return AgnosticEnergyStack(PlatformToolkit.get().commonEnergy, 0)
        if (!predicate.test(firstEnergy)) return AgnosticEnergyStack(PlatformToolkit.get().commonEnergy, 0)
        val extracted = energyService.extractAEPower(limit.toDouble(), if (simulate) Actionable.SIMULATE else Actionable.MODULATE, PowerMultiplier.CONFIG)
        return AgnosticEnergyStack(Energies.REDSTONE_FLUX, extracted.toLong())
    }
}
