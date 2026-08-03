package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEFluidKey
import appeng.api.storage.MEStorage
import appeng.blockentity.grid.AENetworkBlockEntity
import it.unimi.dsi.fastutil.objects.Object2LongMap
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.broccolium.modules.storage.base.api.SomethingOperator
import site.siredvin.broccolium.modules.storage.fluid.AgnosticFluidStack
import site.siredvin.broccolium.modules.storage.fluid.FluidStorageUtils
import site.siredvin.broccolium.modules.storage.fluid.api.AgnosticFluidStorage
import java.util.function.Predicate

class AEFluidStorage(private val storage: MEStorage, private val entity: AENetworkBlockEntity) : AgnosticFluidStorage {
    override fun getContent(): Iterator<AgnosticFluidStack> {
        return storage.availableStacks.mapNotNull {
            if (it.key !is AEFluidKey) return@mapNotNull null
            val fluidKey = it.key as AEFluidKey
            return@mapNotNull AgnosticFluidStack(
                fluidKey.fluid,
                it.longValue.toDouble(),
                fluidKey.tag,
            )
        }.iterator()
    }

    override fun setChanged() {
        entity.setChanged()
    }

    override fun getCapacities(): List<Double> = List(getContent().asSequence().count() + 1, { Double.POSITIVE_INFINITY })
    override val maxStackSize: Double
        get() = Double.MAX_VALUE
    override val operator: SomethingOperator<AgnosticFluidStack, Double>
        get() = FluidStorageUtils

    override fun store(stack: AgnosticFluidStack, simulate: Boolean): AgnosticFluidStack {
        val insertedAmount = storage.insert(AEFluidKeyFactory.of(stack), stack.platformAmount.toLong(), if (simulate) Actionable.SIMULATE else Actionable.MODULATE, IActionSource.ofMachine(entity))
        if (insertedAmount == 0L) return stack
        stack.shrink(insertedAmount.toDouble() / PlatformToolkit.get().fluidCompactDivider)
        return stack
    }

    override fun take(predicate: Predicate<AgnosticFluidStack>, limit: Double, simulate: Boolean): AgnosticFluidStack {
        val platformLimit = limit * PlatformToolkit.get().fluidCompactDivider

        @Suppress("UNCHECKED_CAST")
        val fluidToTransfer = storage.availableStacks.find {
            val aeKey = it.key
            if (aeKey !is AEFluidKey) {
                return@find false
            }
            return@find predicate.test(AgnosticFluidStack(aeKey.fluid, it.longValue.toDouble(), aeKey.tag))
        } as? Object2LongMap.Entry<AEFluidKey> ?: return AgnosticFluidStack.EMPTY
        val extractedAmount = storage.extract(fluidToTransfer.key, minOf(platformLimit.toLong(), fluidToTransfer.longValue), if (simulate) Actionable.SIMULATE else Actionable.MODULATE, IActionSource.ofMachine(entity))
        if (extractedAmount == 0L) return AgnosticFluidStack.EMPTY
        return AgnosticFluidStack(fluidToTransfer.key.fluid, extractedAmount.toDouble(), fluidToTransfer.key.tag)
    }
}
