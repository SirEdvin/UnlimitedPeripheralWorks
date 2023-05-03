package site.siredvin.peripheralworks.integrations.team_reborn_energy

import com.google.common.eventbus.Subscribe
import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.TurtleRefuelHandler
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralium.util.LimitedInventory
import team.reborn.energy.api.EnergyStorage
import java.util.OptionalInt
import kotlin.math.min

object EnergyRefuelHandler: TurtleRefuelHandler {
    override fun refuel(turtle: ITurtleAccess, stack: ItemStack, slot: Int, limit: Int): OptionalInt {
        if (!Configuration.enableTurtleRefulWithEnergy)
            return OptionalInt.empty()
        val inventory = LimitedInventory(turtle.inventory, intArrayOf(slot))
        val storage = InventoryStorage.of(inventory, null)
        val refuelItemSlot = storage.getSlot(0)
        if (refuelItemSlot.isResourceBlank)
            return OptionalInt.empty()
        val context = ContainerItemContext.ofSingleSlot(refuelItemSlot)
        val energyStorage = EnergyStorage.ITEM.find(stack, context) ?: return OptionalInt.empty()
        var extractedAmount = 0L
        Transaction.openOuter().use {
            val realLimit = min(limit.toLong(), energyStorage.amount)
            while (extractedAmount < realLimit && energyStorage.amount != 0L){
                extractedAmount += energyStorage.extract(realLimit - extractedAmount, it)
            }
            val leftEnergy = extractedAmount % Configuration.energyToFuelRate
            if (leftEnergy != 0L) {
                energyStorage.insert(leftEnergy, it)
                extractedAmount -= leftEnergy
            }
            it.commit()
        }
        return OptionalInt.of((extractedAmount / Configuration.energyToFuelRate).toInt())
    }
}