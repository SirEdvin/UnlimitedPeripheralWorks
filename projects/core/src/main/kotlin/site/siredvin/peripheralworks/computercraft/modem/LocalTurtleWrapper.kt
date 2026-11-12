package site.siredvin.peripheralworks.computercraft.modem

import com.mojang.authlib.GameProfile
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.turtle.*
import dan200.computercraft.api.upgrades.UpgradeData
import dan200.computercraft.api.upgrades.UpgradeHandle
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.Container
import net.minecraft.world.level.Level
import site.siredvin.peripheralworks.computercraft.peripherals.turtles.TurtlePeripheraliumHubPeripheral

// The hub keeps nested upgrades on CC's deprecated item-less data API, like Tweakium's upgrade bases.
@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class LocalTurtleWrapper(val access: ITurtleAccess, val tweakedSide: TurtleSide, val upgrade: ITurtleUpgrade, val id: String, private val origin: TurtlePeripheraliumHubPeripheral) : ITurtleAccess {

    // Declared before the peripheral, whose construction may already ask for the handle.
    private val handle = LocalUpgradeHandle(upgrade, origin, { access.updateUpgradeNBTData(tweakedSide) }) { setUpgradeWithData(tweakedSide, null) }

    val peripheral: IPeripheral? = upgrade.createPeripheral(this, tweakedSide)

    val tweakedData: CompoundTag
        get() = getUpgradeNBTData(tweakedSide)

    val upgradeData: UpgradeData<ITurtleUpgrade>
        get() = origin.getUpgradeData(upgrade)

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

    override fun getUpgradeWithData(side: TurtleSide): UpgradeData<ITurtleUpgrade>? = if (side == tweakedSide) upgradeData else access.getUpgradeWithData(side)

    override fun getUpgradeHandle(side: TurtleSide): UpgradeHandle<ITurtleUpgrade>? = if (side == tweakedSide) handle else access.getUpgradeHandle(side)

    override fun setUpgradeWithData(side: TurtleSide, upgrade: UpgradeData<ITurtleUpgrade>?) {
        if (side == tweakedSide) {
            origin.swapUpgrade(upgradeData, upgrade)
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
