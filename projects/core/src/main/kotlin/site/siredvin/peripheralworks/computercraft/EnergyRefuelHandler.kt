package site.siredvin.peripheralworks.computercraft

import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.TurtleRefuelHandler
import net.minecraft.world.item.ItemStack
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStorageLookup
import site.siredvin.broccolium.modules.storage.energy.Energies
import site.siredvin.broccolium.modules.storage.energy.EnergyRegistry
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import java.util.OptionalInt
import kotlin.math.min

object EnergyRefuelHandler : TurtleRefuelHandler {
    override fun refuel(turtle: ITurtleAccess, stack: ItemStack, slot: Int, limit: Int): OptionalInt {
        if (!PeripheralWorksConfig.enableTurtleRefuelWithEnergy) {
            return OptionalInt.empty()
        }
        val itemInSlot = turtle.inventory.getItem(slot)
        val energyStorage = AgnosticEnergyStorageLookup.extractEnergyStorage(turtle.level, itemInSlot) ?: return OptionalInt.empty()
        val energy = energyStorage.energy
        if (energy.isEmpty)
            return OptionalInt.empty()
        if (energy.unit != Energies.TURTLE_FUEL && !EnergyRegistry.isConvertible(energy.unit, Energies.TURTLE_FUEL))
            return OptionalInt.empty()

        val fuelLimit = if (energy.unit == Energies.TURTLE_FUEL) {
            (turtle.fuelLimit - turtle.fuelLevel).coerceAtLeast(0)
        } else {
            (turtle.fuelLimit - turtle.fuelLevel).coerceAtLeast(0) * (1 / EnergyRegistry.CONVERSIONS[energy.unit]!![Energies.TURTLE_FUEL]!!).toInt()
        }

        val realLimit = min(limit, fuelLimit)

        if (realLimit == 0)
            return OptionalInt.empty()

        val extractedEnergy = energyStorage.takeEnergy({ true }, realLimit.toLong())
        if (extractedEnergy.isEmpty)
            return OptionalInt.empty()
        val turtleEnergy = if (extractedEnergy.unit == Energies.TURTLE_FUEL) {
            extractedEnergy
        } else {
            EnergyRegistry.convert(extractedEnergy, Energies.TURTLE_FUEL)
        }
        if (turtleEnergy.isEmpty)
            return OptionalInt.empty()

        return OptionalInt.of(turtleEnergy.amount.toInt())
    }
}