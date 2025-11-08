package site.siredvin.peripheralworks.computercraft

import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.TurtleRefuelHandler
import net.minecraft.world.item.ItemStack
import org.apache.commons.lang3.math.Fraction
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStack
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStorageLookup
import site.siredvin.broccolium.modules.storage.energy.Energies
import site.siredvin.broccolium.modules.storage.energy.EnergyRegistry
import site.siredvin.broccolium.modules.storage.item.ContainerWrapper
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import java.util.OptionalInt
import kotlin.math.min

object EnergyRefuelHandler : TurtleRefuelHandler {
    override fun refuel(turtle: ITurtleAccess, stack: ItemStack, slot: Int, limit: Int): OptionalInt {
        if (!PeripheralWorksConfig.enableTurtleRefuelWithEnergy) {
            return OptionalInt.empty()
        }
        val energyStorage = AgnosticEnergyStorageLookup.extractFromInventoryStack(turtle.level, ContainerWrapper(turtle.inventory), slot) ?: return OptionalInt.empty()
        val energy = energyStorage.energy
        if (energy.isEmpty) {
            return OptionalInt.empty()
        }
        if (energy.unit != Energies.TURTLE_FUEL && !EnergyRegistry.isConvertible(energy.unit, Energies.TURTLE_FUEL)) {
            return OptionalInt.empty()
        }

        val conversionRate = if (energy.unit == Energies.TURTLE_FUEL) {
            Fraction.getFraction(1, 1)
        } else {
            EnergyRegistry.CONVERSIONS[energy.unit]!![Energies.TURTLE_FUEL]!!
        }

        val fuelLimit = ((turtle.fuelLimit - turtle.fuelLevel).coerceAtLeast(0) / conversionRate.toDouble()).toInt()
        val roundedLimit = Fraction.getFraction(conversionRate.multiplyBy(Fraction.getFraction(limit, 1)).toInt(), 1).divideBy(conversionRate).toLong()
        val realLimit = min(roundedLimit.toInt(), fuelLimit)

        if (realLimit == 0) {
            return OptionalInt.empty()
        }

        var slidingLimit = realLimit.toLong()
        val slidingStack = AgnosticEnergyStack(energyStorage.unit, 0)
        for (i in 1..4) {
            val extractedEnergy = energyStorage.takeEnergy({ true }, slidingLimit)
            if (extractedEnergy.isEmpty) {
                if (slidingStack.isEmpty) {
                    return OptionalInt.empty()
                }
                break
            }
            slidingStack.grow(extractedEnergy.amount)
            slidingLimit -= extractedEnergy.amount
            if (slidingLimit <= 0) {
                break
            }
        }

        val turtleEnergy = if (slidingStack.unit == Energies.TURTLE_FUEL) {
            slidingStack
        } else {
            EnergyRegistry.convert(slidingStack, Energies.TURTLE_FUEL)
        }
        if (turtleEnergy.isEmpty) {
            return OptionalInt.empty()
        }

        return OptionalInt.of(turtleEnergy.amount.toInt())
    }
}
