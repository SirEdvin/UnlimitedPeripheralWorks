package site.siredvin.peripheralworks.integrations.modern_industrialization

import aztech.modern_industrialization.api.machine.component.EnergyAccess
import aztech.modern_industrialization.machines.components.EnergyComponent
import aztech.modern_industrialization.util.Simulation
import net.minecraft.network.chat.Component
import site.siredvin.peripheralium.storages.energy.EnergyStack
import site.siredvin.peripheralium.storages.energy.EnergyStorage
import site.siredvin.peripheralium.storages.energy.EnergyUnit
import java.util.function.Predicate

class MIEnergyStorage(private val access: EnergyAccess) : EnergyStorage {
    private val component: EnergyComponent? = access as? EnergyComponent
    companion object {
        val MI_ENERGY = EnergyUnit("EU", Component.literal("Modern industrialization energy"))
    }
    override val capacity: Long
        get() = access.capacity
    override val energy: EnergyStack
        get() = EnergyStack(MI_ENERGY, access.eu)

    override fun setChanged() {
    }

    override fun storeEnergy(stack: EnergyStack): EnergyStack {
        if (component == null || stack.unit != MI_ENERGY) return stack
        val inserted = component.insertEu(stack.amount, Simulation.ACT)
        stack.shrink(inserted)
        return stack
    }

    override fun takeEnergy(predicate: Predicate<EnergyStack>, limit: Long): EnergyStack {
        if (component == null || !predicate.test(energy)) return EnergyStack.EMPTY
        val extracted = component.consumeEu(limit, Simulation.ACT)
        return EnergyStack(MI_ENERGY, extracted)
    }
}
