package site.siredvin.peripheralworks.integrations.ae2

import appeng.core.definitions.AEItems
import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.TurtleSide
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.tweakium.modules.peripheral.OwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.owner.TurtlePeripheralOwner
import site.siredvin.tweakium.modules.turtle.PeripheralTurtleUpgrade

class AE2CraftingMonitorUpgrade(stack: ItemStack) : PeripheralTurtleUpgrade<AE2CraftingMonitorPeripheral>(UPGRADE_ID, stack) {
    override fun buildPeripheral(turtle: ITurtleAccess, side: TurtleSide): AE2CraftingMonitorPeripheral = AE2CraftingMonitorPeripheral.create(turtle, side)

    override fun getUpgradeData(stack: ItemStack): CompoundTag = CompoundTag().apply {
        put(AE2_TERMINAL_TAG, stack.save(CompoundTag()))
    }

    override fun getUpgradeItem(upgradeData: CompoundTag): ItemStack = if (upgradeData.contains(AE2_TERMINAL_TAG, Tag.TAG_COMPOUND.toInt())) {
        ItemStack.of(upgradeData.getCompound(AE2_TERMINAL_TAG))
    } else {
        craftingItem
    }

    override fun isItemSuitable(stack: ItemStack): Boolean = AEItems.WIRELESS_CRAFTING_TERMINAL.isSameAs(stack) &&
        AEItems.WIRELESS_CRAFTING_TERMINAL.asItem().getLinkedPosition(stack) != null

    companion object {
        val UPGRADE_ID = ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, AE2CraftingMonitorPeripheral.TYPE)
    }
}

class AE2CraftingMonitorPeripheral private constructor(owner: TurtlePeripheralOwner) : OwnedPeripheral<TurtlePeripheralOwner>(TYPE, owner) {
    override val isEnabled = true

    init {
        addPlugin(AE2CraftingJobsPlugin.forTurtle(owner))
    }

    companion object {
        const val TYPE = "ae2_crafting_monitor"

        fun create(turtle: ITurtleAccess, side: TurtleSide): AE2CraftingMonitorPeripheral = AE2CraftingMonitorPeripheral(TurtlePeripheralOwner(turtle, side))
    }
}
