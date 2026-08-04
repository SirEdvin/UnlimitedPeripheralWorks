package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.implementations.blockentities.IWirelessAccessPoint
import appeng.api.networking.crafting.ICraftingService
import appeng.api.networking.security.IActionSource
import appeng.api.storage.MEStorage
import appeng.blockentity.networking.WirelessAccessPointBlockEntity
import appeng.core.definitions.AEItems
import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.TurtleSide
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.tweakium.modules.peripheral.OwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import site.siredvin.tweakium.modules.peripheral.boon.PeripheralOwnerBoonKey
import site.siredvin.tweakium.modules.peripheral.owner.TurtlePeripheralOwner
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation
import site.siredvin.tweakium.modules.peripheral.representation.RepresentationMode
import site.siredvin.tweakium.modules.plugins.PeripheralPluginUtils
import site.siredvin.tweakium.modules.turtle.PeripheralTurtleUpgrade
import java.util.Optional
import java.util.function.Predicate
import kotlin.math.min

private const val TERMINAL_TAG = "terminal"

class AE2WirelessTerminalUpgrade(stack: ItemStack) : PeripheralTurtleUpgrade<AE2WirelessTerminalPeripheral>(UPGRADE_ID, stack) {
    override fun buildPeripheral(turtle: ITurtleAccess, side: TurtleSide): AE2WirelessTerminalPeripheral = AE2WirelessTerminalPeripheral.create(turtle, side)

    override fun getUpgradeData(stack: ItemStack): CompoundTag = CompoundTag().apply {
        put(TERMINAL_TAG, stack.save(CompoundTag()))
    }

    override fun getUpgradeItem(upgradeData: CompoundTag): ItemStack = if (upgradeData.contains(TERMINAL_TAG, Tag.TAG_COMPOUND.toInt())) {
        ItemStack.of(upgradeData.getCompound(TERMINAL_TAG))
    } else {
        craftingItem
    }

    override fun isItemSuitable(stack: ItemStack): Boolean = AEItems.WIRELESS_TERMINAL.isSameAs(stack) &&
        AEItems.WIRELESS_TERMINAL.asItem().getLinkedPosition(stack) != null

    companion object {
        val UPGRADE_ID = ResourceLocation(PeripheralWorksCore.MOD_ID, AE2WirelessTerminalPeripheral.TYPE)
    }
}

class AE2WirelessTerminalPeripheral private constructor(owner: TurtlePeripheralOwner) : OwnedPeripheral<TurtlePeripheralOwner>(TYPE, owner) {
    override val isEnabled = true

    init {
        addPlugin(AE2WirelessTerminalPlugin(owner))
    }

    companion object {
        const val TYPE = "ae2_wireless_terminal"

        fun create(turtle: ITurtleAccess, side: TurtleSide): AE2WirelessTerminalPeripheral {
            val owner = TurtlePeripheralOwner(turtle, side).attachFuel()
            return AE2WirelessTerminalPeripheral(owner)
        }
    }
}

private class AE2WirelessTerminalPlugin(private val owner: TurtlePeripheralOwner) : IPeripheralPlugin {
    private data class Session(val storage: MEStorage, val craftingService: ICraftingService)

    private fun resolve(): Session {
        val data = owner.turtle.getUpgradeNBTData(owner.side)
        if (!data.contains(TERMINAL_TAG, Tag.TAG_COMPOUND.toInt())) throw LuaException("Invalid stored wireless terminal")
        val stack = ItemStack.of(data.getCompound(TERMINAL_TAG))
        val terminal = AEItems.WIRELESS_TERMINAL.asItem()
        if (!AEItems.WIRELESS_TERMINAL.isSameAs(stack) || terminal.getLinkedPosition(stack) == null) {
            throw LuaException("Invalid stored wireless terminal")
        }
        val level = owner.level ?: throw LuaException("Linked AE2 network is unavailable")
        val grid = terminal.getLinkedGrid(stack, level, null) ?: throw LuaException("Linked AE2 network is unavailable")
        val inRange = grid.getMachines(WirelessAccessPointBlockEntity::class.java).any { accessPoint ->
            isInRange(accessPoint, level, owner.pos)
        }
        if (!inRange) throw LuaException("Turtle is outside wireless range")
        return Session(grid.storageService.inventory, grid.craftingService)
    }

    private fun validateTransfer(itemQuery: Any?, limit: Optional<Int>, slot: Optional<Int>): Pair<Predicate<ItemStack>, Pair<Int, Int>> {
        val predicate = PeripheralPluginUtils.itemQueryToPredicate(itemQuery)
        val transferLimit = min(PeripheralWorksConfig.itemStorageTransferLimit, limit.orElse(Int.MAX_VALUE))
        if (transferLimit < 0) throw LuaException("Limit must be non-negative")
        val inventorySize = owner.storage!!.size
        val storageSlot = slot.map { it - 1 }.orElse(-1)
        if (storageSlot !in -1 until inventorySize) throw LuaException("Slot must be between 1 and $inventorySize")
        return predicate to (transferLimit to storageSlot)
    }

    private fun validatePush(fromSlotOrItemQuery: Any?, limit: Optional<Int>): Pair<Predicate<ItemStack>, Pair<Int, Int>> {
        if (fromSlotOrItemQuery !is Number) return validateTransfer(fromSlotOrItemQuery, limit, Optional.empty())
        val fromSlot = fromSlotOrItemQuery.toInt()
        if (fromSlotOrItemQuery.toDouble() != fromSlot.toDouble()) throw LuaException("Slot must be an integer")
        return validateTransfer(null, limit, Optional.of(fromSlot))
    }

    private fun consumeFuel() {
        val fuel = owner.getBoon(PeripheralOwnerBoonKey.FUEL)!!
        if (!fuel.consumeFuel(1, false)) throw LuaException("Not enough fuel")
    }

    @LuaFunction(mainThread = true)
    fun getFuelMaxLevel(): Int = owner.getBoon(PeripheralOwnerBoonKey.FUEL)!!.maxFuelLevel

    @LuaFunction(mainThread = true)
    fun items(arguments: IArguments): List<Map<String, *>> {
        val storage = AEItemStorage(resolve().storage, IActionSource.empty()) {}
        val mode = if (arguments.optBoolean(0, true)) RepresentationMode.DETAILED else RepresentationMode.BASE
        val predicate = PeripheralPluginUtils.itemQueryToPredicate(arguments.get(1))
        return storage.getContent().asSequence().filter(predicate::test).map { LuaRepresentation.forItemStack(it, mode) }.toList()
    }

    @LuaFunction(mainThread = true)
    fun pullItem(itemQuery: Any?, limit: Optional<Int>, toSlot: Optional<Int>): Int {
        val session = resolve()
        val (predicate, transfer) = validateTransfer(itemQuery, limit, toSlot)
        consumeFuel()
        return owner.withPlayer({ player ->
            AEItemStorage(session.storage, IActionSource.ofPlayer(player.fakePlayer)) {}
                .moveTo(owner.storage!!, transfer.first, transfer.second, predicate)
        }, skipInventory = true)
    }

    @LuaFunction(mainThread = true)
    fun pushItem(fromSlotOrItemQuery: Any?, limit: Optional<Int>): Int {
        val session = resolve()
        val (predicate, transfer) = validatePush(fromSlotOrItemQuery, limit)
        consumeFuel()
        return owner.withPlayer({ player ->
            owner.storage!!.moveTo(
                AEItemStorage(session.storage, IActionSource.ofPlayer(player.fakePlayer)) {},
                transfer.first,
                transfer.second,
                -1,
                predicate,
            )
        }, skipInventory = true)
    }

    @LuaFunction(mainThread = false)
    fun scheduleCrafting(mode: String, id: String, amount: Optional<Long>, targetCPU: Optional<String>): MethodResult {
        val session = resolve()
        val level = owner.level ?: return MethodResult.of(null, "Linked AE2 network is unavailable")
        return owner.withPlayer({ player ->
            AE2CraftingJobs.schedule(level, session.craftingService, IActionSource.ofPlayer(player.fakePlayer), mode, id, amount, targetCPU)
        }, skipInventory = true)
    }

    @LuaFunction(mainThread = true)
    fun getCraftingJob(jobID: String): MethodResult {
        val service = resolve().craftingService
        return AE2CraftingJobs.get(service, jobID)
    }

    @LuaFunction(mainThread = true)
    fun getCraftingJobs(): List<Map<String, Any>> = AE2CraftingJobs.getAll(resolve().craftingService)

    @LuaFunction(mainThread = true)
    fun cancelCrafting(jobID: String): MethodResult {
        val service = resolve().craftingService
        return AE2CraftingJobs.cancel(service, jobID)
    }

    private fun isInRange(accessPoint: IWirelessAccessPoint, level: net.minecraft.world.level.Level, pos: net.minecraft.core.BlockPos): Boolean = accessPoint.isActive && accessPoint.location.level === level && accessPoint.location.pos.distSqr(pos) < accessPoint.range * accessPoint.range
}
