package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.networking.security.IActionSource
import appeng.core.definitions.AEItems
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.TurtleSide
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.tweakium.modules.peripheral.OwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import site.siredvin.tweakium.modules.peripheral.owner.TurtlePeripheralOwner
import site.siredvin.tweakium.modules.turtle.PeripheralTurtleUpgrade
import java.util.Optional

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
        val UPGRADE_ID = ResourceLocation(PeripheralWorksCore.MOD_ID, AE2CraftingMonitorPeripheral.TYPE)
    }
}

class AE2CraftingMonitorPeripheral private constructor(owner: TurtlePeripheralOwner) : OwnedPeripheral<TurtlePeripheralOwner>(TYPE, owner) {
    override val isEnabled = true

    init {
        addPlugin(AE2CraftingMonitorPlugin(owner))
    }

    companion object {
        const val TYPE = "ae2_crafting_monitor"

        fun create(turtle: ITurtleAccess, side: TurtleSide): AE2CraftingMonitorPeripheral = AE2CraftingMonitorPeripheral(TurtlePeripheralOwner(turtle, side))
    }
}

private class AE2CraftingMonitorPlugin(private val owner: TurtlePeripheralOwner) : IPeripheralPlugin {
    private fun resolve() = resolveWirelessSession(owner, AEItems.WIRELESS_CRAFTING_TERMINAL.asItem())

    @LuaFunction(mainThread = false)
    fun scheduleCrafting(mode: String, id: String, amount: Optional<Long>, targetCPU: Optional<String>): MethodResult {
        val session = resolve()
        val level = owner.level ?: return MethodResult.of(null, "Linked AE2 network is unavailable")
        return owner.withPlayer({ player ->
            AE2CraftingJobs.schedule(level, session.craftingService, IActionSource.ofPlayer(player.fakePlayer), mode, id, amount, targetCPU)
        }, skipInventory = true)
    }

    @LuaFunction(mainThread = true)
    fun getCraftingJob(jobID: String): MethodResult = AE2CraftingJobs.get(resolve().craftingService, jobID)

    @LuaFunction(mainThread = true)
    fun getCraftingJobs(): List<Map<String, Any>> = AE2CraftingJobs.getAll(resolve().craftingService)

    @LuaFunction(mainThread = true)
    fun cancelCrafting(jobID: String): MethodResult = AE2CraftingJobs.cancel(resolve().craftingService, jobID)
}
