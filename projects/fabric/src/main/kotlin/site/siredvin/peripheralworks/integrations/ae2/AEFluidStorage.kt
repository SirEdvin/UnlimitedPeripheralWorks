package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEFluidKey
import appeng.api.storage.MEStorage
import appeng.blockentity.grid.AENetworkBlockEntity
import site.siredvin.peripheralium.storages.fluid.FluidStack
import site.siredvin.peripheralium.storages.fluid.FluidStorage
import site.siredvin.peripheralium.storages.fluid.toVanilla
import site.siredvin.peripheralium.storages.fluid.toVariant
import site.siredvin.peripheralium.xplat.PeripheraliumPlatform
import java.util.function.Predicate

class AEFluidStorage(private val storage: MEStorage, private val entity: AENetworkBlockEntity) : FluidStorage {
    override fun getFluids(): Iterator<FluidStack> {
        return storage.availableStacks.mapNotNull {
            if (it.key !is AEFluidKey) return@mapNotNull null
            return@mapNotNull (it.key as AEFluidKey).toVariant().toVanilla(it.longValue)
        }.iterator()
    }

    override fun setChanged() {
        entity.setChanged()
    }

    override fun storeFluid(stack: FluidStack): FluidStack {
        val insertedAmount = storage.insert(AEFluidKey.of(stack.toVariant()), stack.platformAmount, Actionable.MODULATE, IActionSource.ofMachine(entity))
        if (insertedAmount == 0L) return stack
        stack.shrink(insertedAmount.toInt() / PeripheraliumPlatform.fluidCompactDivider)
        return stack
    }

    override fun takeFluid(predicate: Predicate<FluidStack>, limit: Long): FluidStack {
        val platformLimit = limit * PeripheraliumPlatform.fluidCompactDivider
        val fluidToTransfer = storage.availableStacks.find {
            val aeKey = it.key
            if (aeKey !is AEFluidKey) {
                return@find false
            }
            return@find predicate.test(aeKey.toVariant().toVanilla(it.longValue))
        } ?: return FluidStack.EMPTY
        val extractedAmount = storage.extract(fluidToTransfer.key, minOf(platformLimit, fluidToTransfer.longValue), Actionable.MODULATE, IActionSource.ofMachine(entity))
        if (extractedAmount == 0L) return FluidStack.EMPTY
        return (fluidToTransfer.key as AEFluidKey).toVariant().toVanilla(extractedAmount)
    }
}
