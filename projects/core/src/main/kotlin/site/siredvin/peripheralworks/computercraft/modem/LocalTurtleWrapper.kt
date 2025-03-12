package site.siredvin.peripheralworks.computercraft.modem

import com.mojang.authlib.GameProfile
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.turtle.*
import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.world.Container
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.Level
import site.siredvin.peripheralworks.computercraft.peripherals.turtles.TurtlePeripheraliumHubPeripheral
import kotlin.jvm.optionals.getOrNull

class LocalTurtleWrapper(val access: ITurtleAccess, val tweakedSide: TurtleSide, override val upgrade: Holder.Reference<ITurtleUpgrade>, override val id: String, private val origin: TurtlePeripheraliumHubPeripheral) :
    ITurtleAccess,
    LocalWrapper<ITurtleUpgrade> {

    override val peripheral: IPeripheral? = upgrade.value().createPeripheral(this, tweakedSide)

    val tweakedData: DataComponentPatch
        get() = getUpgradeData(tweakedSide)

    override val fullUpgradeData: UpgradeData<ITurtleUpgrade>
        get() = UpgradeData.of(upgrade, tweakedData)

    override fun getLevel(): Level = access.level

    override fun getPosition(): BlockPos = access.position

    override fun isRemoved(): Boolean = access.isRemoved

    override fun teleportTo(world: Level, pos: BlockPos): Boolean = access.teleportTo(world, pos)

    override fun getDirection(): Direction = access.direction

    override fun setDirection(dir: Direction) {
        access.direction = dir
    }

    override fun getSelectedSlot(): Int = access.selectedSlot

    override fun setSelectedSlot(slot: Int) {
        access.selectedSlot = slot
    }

    override fun setColour(colour: Int) {
        access.colour = colour
    }

    override fun getColour(): Int = access.colour

    override fun getOwningPlayer(): GameProfile? = access.owningPlayer

    override fun getInventory(): Container = access.inventory

    override fun isFuelNeeded(): Boolean = access.isFuelNeeded

    override fun getFuelLevel(): Int = access.fuelLevel

    override fun setFuelLevel(fuel: Int) {
        access.fuelLevel = fuel
    }

    override fun getFuelLimit(): Int = access.fuelLimit

    override fun consumeFuel(fuel: Int): Boolean = access.consumeFuel(fuel)

    override fun addFuel(fuel: Int) {
        access.addFuel(fuel)
    }

    override fun executeCommand(command: TurtleCommand): MethodResult = access.executeCommand(command)

    override fun playAnimation(animation: TurtleAnimation) = access.playAnimation(animation)

    override fun getUpgrade(side: TurtleSide): ITurtleUpgrade? {
        if (side == tweakedSide) {
            return upgrade.value()
        }
        return access.getUpgrade(side)
    }

    override fun setUpgrade(side: TurtleSide, upgrade: UpgradeData<ITurtleUpgrade>?) {
        if (side == tweakedSide) {
            if (upgrade == null) {
                origin.swapUpgrade(UpgradeData.of(this.upgrade, tweakedData), null)
            } else {
                origin.swapUpgrade(UpgradeData.of(this.upgrade, tweakedData), upgrade)
            }
        } else {
            @Suppress("DEPRECATION")
            access.setUpgrade(side, upgrade)
        }
    }

    override fun setUpgradeData(side: TurtleSide, data: DataComponentPatch) {
        if (side == tweakedSide) {
            origin.setDataForUpdate(id, data.get(DataComponents.CUSTOM_DATA)?.getOrNull()?.copyTag())
        } else {
            access.setUpgradeData(side, data)
        }
    }

    override fun getUpgradeWithData(side: TurtleSide?): UpgradeData<ITurtleUpgrade>? {
        if (side == tweakedSide) {
            return fullUpgradeData
        }
        return access.getUpgradeWithData(side)
    }

    override fun getPeripheral(side: TurtleSide): IPeripheral? {
        if (side == tweakedSide) {
            return peripheral
        }
        return access.getPeripheral(side)
    }

    override fun getUpgradeData(side: TurtleSide): DataComponentPatch {
        val base = access.getUpgradeData(side)
        if (side == tweakedSide) {
            return DataComponentPatch.builder().set(DataComponents.CUSTOM_DATA, CustomData.of(origin.getDataForUpgrade(id))).build()
        }
        return base
    }
}
