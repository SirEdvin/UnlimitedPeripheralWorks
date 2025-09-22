package site.siredvin.peripheralworks.integrations.ars_nouveau

import com.hollingsworth.arsnouveau.api.source.ISourceTile
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStack
import site.siredvin.broccolium.modules.storage.energy.EnergyUnit
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorage
import site.siredvin.peripheralworks.common.setup.ModEnergies
import java.util.function.Predicate

class AgnosticSourceStorage(private val sourceTile: ISourceTile) : AgnosticEnergyStorage {
    override val energy: AgnosticEnergyStack
        get() = AgnosticEnergyStack(unit, sourceTile.source.toLong())
    override val capacity: Long
        get() = sourceTile.maxSource.toLong()
    override val canExtract: Boolean
        get() = true

    override fun takeEnergy(
        predicate: Predicate<AgnosticEnergyStack>,
        limit: Long,
    ): AgnosticEnergyStack {
        if (!predicate.test(energy)) return AgnosticEnergyStack(unit, 0)
        val actualLimit = limit.toInt().coerceAtMost(sourceTile.source)
        sourceTile.source -= actualLimit
        return AgnosticEnergyStack(unit, actualLimit.toLong())
    }

    override val canReceive: Boolean
        get() = sourceTile.canAcceptSource()
    override val unit: EnergyUnit
        get() = ModEnergies.SOURCE

    override fun storeEnergy(stack: AgnosticEnergyStack): AgnosticEnergyStack {
        if (!stack.`is`(unit)) return stack
        val actualLimit = stack.amount.toInt().coerceAtMost(sourceTile.maxSource - sourceTile.source)
        sourceTile.source += actualLimit
        return AgnosticEnergyStack(unit, stack.amount - actualLimit.toLong())
    }

    override fun setChanged() {
    }
}
