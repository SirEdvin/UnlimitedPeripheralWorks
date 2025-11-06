package site.siredvin.peripheralworks.integrations.powah

import owmii.powah.lib.block.AbstractEnergyBlock
import owmii.powah.lib.block.AbstractEnergyStorage
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStack
import site.siredvin.broccolium.modules.storage.energy.EnergyUnit
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorage
import site.siredvin.peripheralworks.xplat.ModPlatform
import java.util.function.Predicate

class PowahEnergyStorageWrapper(private val storage: AbstractEnergyStorage<*, *>) : AgnosticEnergyStorage {
    override val capacity: Long
        get() = storage.energy.capacity
    override val canExtract: Boolean
        get() = storage.canExtractEnergy(null)
    override val energy: AgnosticEnergyStack
        get() = AgnosticEnergyStack(ModPlatform.commonEnergy, storage.energy.stored)

    override val receiveRateLimit: Long
        get() = (storage.block as? AbstractEnergyBlock)?.config?.getTransfer(storage.variant) ?: Long.MAX_VALUE

    override val extractRateLimit: Long
        get() = receiveRateLimit

    override fun setChanged() {
        storage.setChanged()
    }

    override val canReceive: Boolean
        get() = storage.canReceiveEnergy(null)
    override val unit: EnergyUnit
        get() = ModPlatform.commonEnergy

    override fun storeEnergy(stack: AgnosticEnergyStack): AgnosticEnergyStack {
        if (!stack.`is`(ModPlatform.commonEnergy)) return stack
        if (!storage.canReceiveEnergy(null)) return stack
        val receivedEnergy = storage.receiveEnergy(stack.amount, false, null)
        return stack.copyWithCount(stack.amount - receivedEnergy)
    }

    override fun takeEnergy(predicate: Predicate<AgnosticEnergyStack>, limit: Long): AgnosticEnergyStack {
        if (!predicate.test(energy)) return AgnosticEnergyStack(ModPlatform.commonEnergy, 0)
        val extractedEnergy = storage.extractEnergy(limit, false, null)
        if (extractedEnergy == 0L) return AgnosticEnergyStack(ModPlatform.commonEnergy, 0)
        return AgnosticEnergyStack(ModPlatform.commonEnergy, extractedEnergy)
    }
}
