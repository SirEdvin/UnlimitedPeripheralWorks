package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEFluidKey
import appeng.api.stacks.AEKeyType
import appeng.api.storage.IStorageProvider
import appeng.api.storage.MEStorage
import appeng.api.storage.cells.IBasicCellItem
import appeng.blockentity.AEBaseInvBlockEntity
import appeng.blockentity.grid.AENetworkBlockEntity
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.broccolium.modules.storage.fluid.AgnosticFluidStack
import site.siredvin.broccolium.modules.storage.fluid.api.AgnosticFluidStorage
import site.siredvin.broccolium.modules.storage.fluid.toForge
import site.siredvin.broccolium.modules.storage.fluid.toVanilla
import java.util.function.Predicate

class AEFluidStorage(private val storage: MEStorage, private val entity: AENetworkBlockEntity) : AgnosticFluidStorage {
    override fun getFluids(): Iterator<AgnosticFluidStack> {
        return storage.availableStacks.mapNotNull {
            if (it.key !is AEFluidKey) return@mapNotNull null
            return@mapNotNull (it.key as AEFluidKey).toStack(it.longValue.toInt()).toVanilla()
        }.iterator()
    }

    override fun setChanged() {
        entity.setChanged()
    }

    override fun getCapacities(): List<Double> {
        return List(getFluids().asSequence().count() + 1, { Double.POSITIVE_INFINITY })
    }

    override fun storeFluid(stack: AgnosticFluidStack): AgnosticFluidStack {
        val insertedAmount = storage.insert(AEFluidKey.of(stack.toForge()), stack.platformAmount.toLong(), Actionable.MODULATE, IActionSource.ofMachine(entity))
        if (insertedAmount == 0L) return stack
        stack.shrink(insertedAmount.toDouble() / PlatformToolkit.get().fluidCompactDivider)
        return stack
    }

    override fun takeFluid(predicate: Predicate<AgnosticFluidStack>, limit: Double): AgnosticFluidStack {
        val platformLimit = limit * PlatformToolkit.get().fluidCompactDivider
        val fluidToTransfer = storage.availableStacks.find {
            val aeKey = it.key
            if (aeKey !is AEFluidKey) {
                return@find false
            }
            return@find predicate.test(aeKey.toStack(it.longValue.toInt()).toVanilla())
        } ?: return AgnosticFluidStack.EMPTY
        val extractedAmount = storage.extract(fluidToTransfer.key, minOf(platformLimit.toLong(), fluidToTransfer.longValue), Actionable.MODULATE, IActionSource.ofMachine(entity))
        if (extractedAmount == 0L) return AgnosticFluidStack.EMPTY
        return (fluidToTransfer.key as AEFluidKey).toStack(extractedAmount.toInt()).toVanilla()
    }
}
