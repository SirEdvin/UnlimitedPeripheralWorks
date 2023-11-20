package site.siredvin.peripheralworks.integrations.team_reborn_energy

import com.google.common.eventbus.Subscribe
import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.event.TurtleRefuelEvent
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralium.util.LimitedInventory
import team.reborn.energy.api.EnergyStorage
import kotlin.math.min

object EnergyRefuelHandler : TurtleRefuelEvent.Handler {
    override fun refuel(turtle: ITurtleAccess, stack: ItemStack, slot: Int, limit: Int): Int {
        val inventory = LimitedInventory(turtle.inventory, intArrayOf(slot))
        val storage = InventoryStorage.of(inventory, null)
        val refuelItemSlot = storage.getSlot(0)
        val context = ContainerItemContext.ofSingleSlot(refuelItemSlot)
        val energyStorage = EnergyStorage.ITEM.find(stack, context) ?: return 0
        var extractedAmount = 0L
        Transaction.openOuter().use {
            val realLimit = min(limit.toLong(), energyStorage.amount)
            while (extractedAmount < realLimit && energyStorage.amount != 0L) {
                extractedAmount += energyStorage.extract(realLimit - extractedAmount, it)
            }
            val leftEnergy = extractedAmount % Configuration.energyToFuelRate
            if (leftEnergy != 0L) {
                energyStorage.insert(leftEnergy, it)
                extractedAmount -= leftEnergy
            }
            it.commit()
        }
        return (extractedAmount / Configuration.energyToFuelRate).toInt()
    }

    @Subscribe
    fun onTurtleRefuel(event: TurtleRefuelEvent) {
        if (event.handler == null && Configuration.enableTurtleRefulWithEnergy) {
            val storage = EnergyStorage.ITEM.find(event.stack, ContainerItemContext.withConstant(event.stack))
            if (storage != null && storage.amount > 0) {
                event.handler = this
            }
        }
    }
}
