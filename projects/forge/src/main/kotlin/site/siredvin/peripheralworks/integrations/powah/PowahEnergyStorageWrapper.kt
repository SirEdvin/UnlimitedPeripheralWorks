package site.siredvin.peripheralworks.integrations.powah

import owmii.powah.lib.block.AbstractEnergyStorage
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStack
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorage
import site.siredvin.peripheralworks.xplat.ModPlatform
import java.util.function.Predicate

class PowahEnergyStorageWrapper(private val storage: AbstractEnergyStorage<*, *>) : AgnosticEnergyStorage {
    override val capacity: Long
        get() = storage.energy.capacity
    override val energy: AgnosticEnergyStack
        get() = AgnosticEnergyStack(ModPlatform.commonEnergy, storage.energy.stored)

    override fun setChanged() {
        storage.setChanged()
    }

    override fun storeEnergy(stack: AgnosticEnergyStack): AgnosticEnergyStack {
        if (!stack.`is`(ModPlatform.commonEnergy)) return stack
        if (!storage.canReceiveEnergy(null)) return stack
        val receivedEnergy = storage.receiveEnergy(stack.amount, false, null)
        return stack.copyWithCount(stack.amount - receivedEnergy)
    }

    override fun takeEnergy(predicate: Predicate<AgnosticEnergyStack>, limit: Long): AgnosticEnergyStack {
        if (!predicate.test(energy)) return AgnosticEnergyStack.EMPTY
        val extractedEnergy = storage.extractEnergy(limit, false, null)
        if (extractedEnergy == 0L) return AgnosticEnergyStack.EMPTY
        return AgnosticEnergyStack(ModPlatform.commonEnergy, extractedEnergy)
    }
}
