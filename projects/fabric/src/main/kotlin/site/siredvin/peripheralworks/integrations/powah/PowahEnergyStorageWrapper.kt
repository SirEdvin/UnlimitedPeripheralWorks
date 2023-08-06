package site.siredvin.peripheralworks.integrations.powah

import owmii.powah.lib.block.AbstractEnergyStorage
import site.siredvin.peripheralium.storages.energy.EnergyStack
import site.siredvin.peripheralium.storages.energy.EnergyStorage
import site.siredvin.peripheralworks.xplat.ModPlatform
import java.util.function.Predicate

class PowahEnergyStorageWrapper(private val storage: AbstractEnergyStorage<*, *>) : EnergyStorage {
    override val capacity: Long
        get() = storage.energy.capacity
    override val energy: EnergyStack
        get() = EnergyStack(ModPlatform.commonEnergy, storage.energy.stored)

    override fun setChanged() {
        storage.setChanged()
    }

    override fun storeEnergy(stack: EnergyStack): EnergyStack {
        if (!stack.`is`(ModPlatform.commonEnergy)) return stack
        if (!storage.canReceiveEnergy(null)) return stack
        val receivedEnergy = storage.receiveEnergy(stack.amount, false, null)
        return stack.copyWithCount(stack.amount - receivedEnergy)
    }

    override fun takeEnergy(predicate: Predicate<EnergyStack>, limit: Long): EnergyStack {
        if (!predicate.test(energy)) return EnergyStack.EMPTY
        val extractedEnergy = storage.extractEnergy(limit, false, null)
        if (extractedEnergy == 0L) return EnergyStack.EMPTY
        return EnergyStack(ModPlatform.commonEnergy, extractedEnergy)
    }
}
