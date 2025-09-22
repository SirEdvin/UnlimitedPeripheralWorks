package site.siredvin.peripheralworks.integrations.modern_industrialization

import aztech.modern_industrialization.api.machine.component.EnergyAccess
import aztech.modern_industrialization.machines.components.EnergyComponent
import aztech.modern_industrialization.util.Simulation
import net.minecraft.network.chat.Component
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStack
import site.siredvin.broccolium.modules.storage.energy.EnergyRegistry
import site.siredvin.broccolium.modules.storage.energy.EnergyUnit
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorage
import java.util.function.Predicate

class MIEnergyStorage(private val access: EnergyAccess) : AgnosticEnergyStorage {
    private val component: EnergyComponent? = access as? EnergyComponent
    companion object {
        val MI_ENERGY = EnergyRegistry.register("EU", Component.literal("Modern industrialization energy"))
    }
    override val capacity: Long
        get() = access.capacity
    override val canExtract: Boolean
        get() = true
    override val energy: AgnosticEnergyStack
        get() = AgnosticEnergyStack(MI_ENERGY, access.eu)

    override fun setChanged() {
    }

    override val canReceive: Boolean
        get() = true
    override val unit: EnergyUnit
        get() = MI_ENERGY

    override fun storeEnergy(stack: AgnosticEnergyStack): AgnosticEnergyStack {
        if (component == null || stack.unit != MI_ENERGY) return stack
        val inserted = component.insertEu(stack.amount, Simulation.ACT)
        stack.shrink(inserted)
        return stack
    }

    override fun takeEnergy(predicate: Predicate<AgnosticEnergyStack>, limit: Long): AgnosticEnergyStack {
        if (component == null || !predicate.test(energy)) return AgnosticEnergyStack(unit, 0)
        val extracted = component.consumeEu(limit, Simulation.ACT)
        return AgnosticEnergyStack(MI_ENERGY, extracted)
    }
}
