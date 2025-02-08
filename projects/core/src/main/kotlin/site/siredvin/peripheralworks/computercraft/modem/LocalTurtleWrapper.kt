package site.siredvin.peripheralworks.computercraft.modem

import com.mojang.authlib.GameProfile
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.turtle.*
import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.Container
import net.minecraft.world.level.Level
import site.siredvin.peripheralworks.computercraft.peripherals.turtles.TurtlePeripheraliumHubPeripheral

class LocalTurtleWrapper(val access: ITurtleAccess, val tweakedSide: TurtleSide, val upgrade: ITurtleUpgrade, val id: String, private val origin: TurtlePeripheraliumHubPeripheral) : ITurtleAccess {

    val peripheral: IPeripheral? = upgrade.createPeripheral(this, tweakedSide)

    val tweakedData: CompoundTag
        get() = getUpgradeNBTData(tweakedSide)

    val upgradeData: UpgradeData<ITurtleUpgrade>
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
            return upgrade
        }
        return access.getUpgrade(side)
    }

    @Deprecated("Deprecated in Java")
    override fun setUpgrade(side: TurtleSide, upgrade: ITurtleUpgrade?) {
        if (side == tweakedSide) {
            origin.swapUpgrade(UpgradeData.ofDefault(this.upgrade), UpgradeData.ofDefault(upgrade))
        } else {
            @Suppress("DEPRECATION")
            access.setUpgrade(side, upgrade)
        }
    }

    override fun setUpgradeWithData(side: TurtleSide?, upgrade: UpgradeData<ITurtleUpgrade>?) {
        if (side == tweakedSide) {
            if (upgrade == null) {
                origin.swapUpgrade(UpgradeData.of(this.upgrade, tweakedData), null)
            } else {
                origin.swapUpgrade(UpgradeData.of(this.upgrade, tweakedData), upgrade)
            }
        } else {
            access.setUpgradeWithData(side, upgrade)
        }
    }

    override fun getPeripheral(side: TurtleSide): IPeripheral? {
        if (side == tweakedSide) {
            return peripheral
        }
        return access.getPeripheral(side)
    }

    override fun getUpgradeNBTData(side: TurtleSide): CompoundTag {
        val base = access.getUpgradeNBTData(side)
        if (side == tweakedSide) {
            return origin.getDataForUpgrade(id)
        }
        return base
    }

    override fun updateUpgradeNBTData(side: TurtleSide) {
        access.updateUpgradeNBTData(side)
    }
}
