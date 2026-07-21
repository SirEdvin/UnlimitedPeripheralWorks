package site.siredvin.peripheralworks.integrations.modern_industrialization

import aztech.modern_industrialization.api.machine.component.EnergyAccess
import aztech.modern_industrialization.machines.components.EnergyComponent
import aztech.modern_industrialization.util.Simulation
import net.minecraft.network.chat.Component
import site.siredvin.broccolium.modules.storage.base.api.SomethingOperator
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStack
import site.siredvin.broccolium.modules.storage.energy.EnergyRegistry
import site.siredvin.broccolium.modules.storage.energy.EnergyStorageUtils
import site.siredvin.broccolium.modules.storage.energy.EnergyUnit
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorage
import java.util.function.Predicate

class MIEnergyStorage(private val access: EnergyAccess) : AgnosticEnergyStorage {
    private val component: EnergyComponent? = access as? EnergyComponent
    companion object {
        val MI_ENERGY = EnergyRegistry.register("EU", Component.literal("Modern industrialization energy"))
    }
    override val maxStackSize: Long
        get() = access.capacity
    override val operator: SomethingOperator<AgnosticEnergyStack, Long>
        get() = EnergyStorageUtils
    override val canExtract: Boolean
        get() = true
    override val firstEnergy: AgnosticEnergyStack
        get() = AgnosticEnergyStack(MI_ENERGY, access.eu)

    override fun setChanged() {
    }

    override val canReceive: Boolean
        get() = true
    val unit: EnergyUnit
        get() = MI_ENERGY

    override fun store(stack: AgnosticEnergyStack, simulate: Boolean): AgnosticEnergyStack {
        if (component == null || stack.unit != MI_ENERGY) return stack
        val inserted = component.insertEu(stack.amount, if (simulate) Simulation.SIMULATE else Simulation.ACT)
        stack.shrink(inserted)
        return stack
    }

    override fun getContent(): Iterator<AgnosticEnergyStack> = listOf(firstEnergy).iterator()

    override fun take(predicate: Predicate<AgnosticEnergyStack>, limit: Long, simulate: Boolean): AgnosticEnergyStack {
        if (component == null || !predicate.test(firstEnergy)) return AgnosticEnergyStack(unit, 0)
        val extracted = component.consumeEu(limit, if (simulate) Simulation.SIMULATE else Simulation.ACT)
        return AgnosticEnergyStack(MI_ENERGY, extracted)
    }
}
