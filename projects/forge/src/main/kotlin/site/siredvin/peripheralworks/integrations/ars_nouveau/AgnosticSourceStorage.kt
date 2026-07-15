package site.siredvin.peripheralworks.integrations.ars_nouveau

import com.hollingsworth.arsnouveau.api.source.ISourceTile
import site.siredvin.broccolium.modules.storage.base.api.SomethingOperator
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStack
import site.siredvin.broccolium.modules.storage.energy.EnergyStorageUtils
import site.siredvin.broccolium.modules.storage.energy.EnergyUnit
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorage
import site.siredvin.peripheralworks.common.setup.ModEnergies
import java.util.function.Predicate

class AgnosticSourceStorage(private val sourceTile: ISourceTile) : AgnosticEnergyStorage {
    override val firstEnergy: AgnosticEnergyStack
        get() = AgnosticEnergyStack(unit, sourceTile.source.toLong())
    override val maxStackSize: Long
        get() = sourceTile.maxSource.toLong()
    override val operator: SomethingOperator<AgnosticEnergyStack, Long>
        get() = EnergyStorageUtils
    override val canExtract: Boolean
        get() = true

    override fun getContent(): Iterator<AgnosticEnergyStack> = listOf(firstEnergy).iterator()

    override fun take(
        predicate: Predicate<AgnosticEnergyStack>,
        limit: Long,
        simulate: Boolean,
    ): AgnosticEnergyStack {
        if (!predicate.test(firstEnergy)) return AgnosticEnergyStack(unit, 0)
        val actualLimit = limit.toInt().coerceAtMost(sourceTile.source)
        if (!simulate) {
            sourceTile.source -= actualLimit
        }
        return AgnosticEnergyStack(unit, actualLimit.toLong())
    }

    override val canReceive: Boolean
        get() = sourceTile.canAcceptSource()
    val unit: EnergyUnit
        get() = ModEnergies.SOURCE

    override fun store(stack: AgnosticEnergyStack, simulate: Boolean): AgnosticEnergyStack {
        if (!stack.`is`(unit)) return stack
        val actualLimit = stack.amount.toInt().coerceAtMost(sourceTile.maxSource - sourceTile.source)
        if (!simulate) {
            sourceTile.source += actualLimit
        }
        return AgnosticEnergyStack(unit, stack.amount - actualLimit.toLong())
    }

    override fun setChanged() {
    }
}
