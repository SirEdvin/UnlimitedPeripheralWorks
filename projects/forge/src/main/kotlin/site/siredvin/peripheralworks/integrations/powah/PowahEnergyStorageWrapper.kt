package site.siredvin.peripheralworks.integrations.powah

import owmii.powah.lib.block.AbstractEnergyBlock
import owmii.powah.lib.block.AbstractEnergyStorage
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.broccolium.modules.storage.base.api.SomethingOperator
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStack
import site.siredvin.broccolium.modules.storage.energy.Energies
import site.siredvin.broccolium.modules.storage.energy.EnergyStorageUtils
import site.siredvin.broccolium.modules.storage.energy.EnergyUnit
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorage
import java.util.function.Predicate

class PowahEnergyStorageWrapper(private val storage: AbstractEnergyStorage<*, *>) : AgnosticEnergyStorage {
    override val maxStackSize: Long
        get() = storage.energy.capacity
    override val operator: SomethingOperator<AgnosticEnergyStack, Long>
        get() = EnergyStorageUtils
    override val canExtract: Boolean
        get() = storage.canExtractEnergy(null)
    override val firstEnergy: AgnosticEnergyStack
        get() = AgnosticEnergyStack(PlatformToolkit.get().commonEnergy, storage.energy.stored)

    override val receiveRateLimit: Long
        get() = (storage.block as? AbstractEnergyBlock)?.config?.getTransfer(storage.variant) ?: Long.MAX_VALUE

    override val extractRateLimit: Long
        get() = receiveRateLimit

    override fun setChanged() {
        storage.setChanged()
    }

    override val canReceive: Boolean
        get() = storage.canReceiveEnergy(null)
    val unit: EnergyUnit
        get() = PlatformToolkit.get().commonEnergy

    override fun store(stack: AgnosticEnergyStack, simulate: Boolean): AgnosticEnergyStack {
        if (!stack.`is`(PlatformToolkit.get().commonEnergy)) return stack
        if (!storage.canReceiveEnergy(null)) return stack
        val receivedEnergy = storage.receiveEnergy(stack.amount, simulate, null)
        return stack.copyWithCount(stack.amount - receivedEnergy)
    }

    override fun getContent(): Iterator<AgnosticEnergyStack> {
        return listOf(firstEnergy).iterator()
    }

    override fun take(predicate: Predicate<AgnosticEnergyStack>, limit: Long, simulate: Boolean): AgnosticEnergyStack {
        if (!predicate.test(firstEnergy)) return AgnosticEnergyStack(PlatformToolkit.get().commonEnergy, 0)
        val extractedEnergy = storage.extractEnergy(limit, simulate, null)
        if (extractedEnergy == 0L) return AgnosticEnergyStack(PlatformToolkit.get().commonEnergy, 0)
        return AgnosticEnergyStack(PlatformToolkit.get().commonEnergy, extractedEnergy)
    }
}
