package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEFluidKey
import appeng.api.storage.MEStorage
import appeng.blockentity.grid.AENetworkBlockEntity
import site.siredvin.peripheralium.storages.fluid.*
import java.util.function.Predicate

class AEFluidStorage(private val storage: MEStorage, private val entity: AENetworkBlockEntity) : FluidStorage {
    override fun getFluids(): Iterator<FluidStack> {
        return storage.availableStacks.mapNotNull {
            if (it.key !is AEFluidKey) return@mapNotNull null
            return@mapNotNull (it.key as AEFluidKey).toStack(it.longValue.toInt()).toVanilla()
        }.iterator()
    }

    override fun setChanged() {
        entity.setChanged()
    }

    override fun storeFluid(stack: FluidStack): FluidStack {
        val insertedAmount = storage.insert(AEFluidKey.of(stack.toForge()), stack.amount, Actionable.MODULATE, IActionSource.ofMachine(entity))
        if (insertedAmount == 0L) return stack
        stack.shrink(insertedAmount.toInt())
        return stack
    }

    override fun takeFluid(predicate: Predicate<FluidStack>, limit: Long): FluidStack {
        val fluidToTransfer = storage.availableStacks.find {
            val aeKey = it.key
            if (aeKey !is AEFluidKey) {
                return@find false
            }
            return@find predicate.test(aeKey.toStack(it.longValue.toInt()).toVanilla())
        } ?: return FluidStack.EMPTY
        val extractedAmount = storage.extract(fluidToTransfer.key, minOf(limit, fluidToTransfer.longValue), Actionable.MODULATE, IActionSource.ofMachine(entity))
        if (extractedAmount == 0L) return FluidStack.EMPTY
        return (fluidToTransfer.key as AEFluidKey).toStack(extractedAmount.toInt()).toVanilla()
    }
}
