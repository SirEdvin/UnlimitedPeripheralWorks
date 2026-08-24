package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.implementations.blockentities.IWirelessAccessPoint
import appeng.api.networking.IGridNode
import appeng.api.networking.crafting.ICraftingService
import appeng.api.networking.security.IActionSource
import appeng.api.storage.MEStorage
import appeng.blockentity.networking.WirelessAccessPointBlockEntity
import appeng.items.tools.powered.WirelessTerminalItem
import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.pocket.IPocketAccess
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
import site.siredvin.tweakium.modules.peripheral.owner.BasePeripheralOwner
import site.siredvin.tweakium.modules.peripheral.owner.PocketPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.owner.TurtlePeripheralOwner
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation
import site.siredvin.tweakium.modules.peripheral.representation.RepresentationMode
import site.siredvin.tweakium.modules.plugins.PeripheralPluginUtils
import site.siredvin.tweakium.modules.pocket.BasePocketUpgrade
import site.siredvin.tweakium.modules.turtle.PeripheralTurtleUpgrade
import java.util.Optional
import java.util.function.Predicate
import kotlin.math.min

internal const val AE2_TERMINAL_TAG = "terminal"

internal data class AE2WirelessSession(
    val storage: MEStorage,
    val craftingService: ICraftingService,
    val accessPointNode: IGridNode,
)

private fun wirelessTerminalStack(owner: BasePeripheralOwner): ItemStack {
    val data = when (owner) {
        is TurtlePeripheralOwner -> owner.turtle.getUpgradeNBTData(owner.side)
        is PocketPeripheralOwner -> owner.pocket.upgradeNBTData
        else -> throw LuaException("Invalid wireless terminal owner")
    }
    if (!data.contains(AE2_TERMINAL_TAG, Tag.TAG_COMPOUND.toInt())) throw LuaException("Invalid stored wireless terminal")
    return ItemStack.of(data.getCompound(AE2_TERMINAL_TAG))
}

internal fun resolveWirelessSession(owner: BasePeripheralOwner): AE2WirelessSession {
    val stack = wirelessTerminalStack(owner)
    val terminal = stack.item as? WirelessTerminalItem ?: throw LuaException("Invalid stored wireless terminal")
    if (terminal.getLinkedPosition(stack) == null) throw LuaException("Invalid stored wireless terminal")
    val level = owner.level ?: throw LuaException("Linked AE2 network is unavailable")
    val grid = terminal.getLinkedGrid(stack, level, null) ?: throw LuaException("Linked AE2 network is unavailable")
    val accessPoint = grid.getMachines(WirelessAccessPointBlockEntity::class.java).firstOrNull { accessPoint ->
        isInWirelessRange(accessPoint, level, owner.pos)
    } ?: throw LuaException("Computer is outside wireless range")
    return AE2WirelessSession(
        grid.storageService.inventory,
        grid.craftingService,
        accessPoint.mainNode.node ?: throw LuaException("Linked AE2 network is unavailable"),
    )
}

private fun isInWirelessRange(accessPoint: IWirelessAccessPoint, level: net.minecraft.world.level.Level, pos: net.minecraft.core.BlockPos): Boolean = accessPoint.isActive && accessPoint.location.level === level && accessPoint.location.pos.distSqr(pos) < accessPoint.range * accessPoint.range

private fun wirelessTerminalData(stack: ItemStack): CompoundTag = CompoundTag().apply {
    put(AE2_TERMINAL_TAG, stack.save(CompoundTag()))
}

private fun wirelessTerminalItem(data: CompoundTag, fallback: ItemStack): ItemStack = if (data.contains(AE2_TERMINAL_TAG, Tag.TAG_COMPOUND.toInt())) ItemStack.of(data.getCompound(AE2_TERMINAL_TAG)) else fallback

private fun isLinkedWirelessTerminal(stack: ItemStack): Boolean = (stack.item as? WirelessTerminalItem)?.getLinkedPosition(stack) != null

class AE2WirelessTerminalUpgrade(id: ResourceLocation, stack: ItemStack) : PeripheralTurtleUpgrade<AE2WirelessTerminalPeripheral>(id, stack) {
    override fun buildPeripheral(turtle: ITurtleAccess, side: TurtleSide): AE2WirelessTerminalPeripheral = AE2WirelessTerminalPeripheral.create(turtle, side)

    override fun update(turtle: ITurtleAccess, side: TurtleSide) {
        (turtle.getPeripheral(side) as? AE2WirelessTerminalPeripheral)?.updateSubscriptions()
    }

    override fun getUpgradeData(stack: ItemStack): CompoundTag = wirelessTerminalData(stack)

    override fun getUpgradeItem(upgradeData: CompoundTag): ItemStack = wirelessTerminalItem(upgradeData, craftingItem)

    override fun isItemSuitable(stack: ItemStack): Boolean = isLinkedWirelessTerminal(stack)

    companion object {
        @Suppress("DEPRECATION")
        val UPGRADE_ID = ResourceLocation(PeripheralWorksCore.MOD_ID, AE2WirelessTerminalPeripheral.TYPE)

        @Suppress("DEPRECATION")
        val CRAFTING_UPGRADE_ID = ResourceLocation(PeripheralWorksCore.MOD_ID, "ae2_wireless_crafting_terminal")
    }
}

class AE2WirelessTerminalPocketUpgrade(id: ResourceLocation, stack: ItemStack) : BasePocketUpgrade<AE2WirelessTerminalPeripheral>(id, stack) {
    override fun getPeripheral(access: IPocketAccess): AE2WirelessTerminalPeripheral = AE2WirelessTerminalPeripheral.create(access)

    override fun update(access: IPocketAccess, peripheral: IPeripheral?) {
        (peripheral as? AE2WirelessTerminalPeripheral)?.updateSubscriptions()
    }

    override fun getUpgradeData(stack: ItemStack): CompoundTag = wirelessTerminalData(stack)

    override fun getUpgradeItem(upgradeData: CompoundTag): ItemStack = wirelessTerminalItem(upgradeData, craftingItem)

    override fun isItemSuitable(stack: ItemStack): Boolean = isLinkedWirelessTerminal(stack)
}

class AE2WirelessTerminalPeripheral private constructor(owner: BasePeripheralOwner) : OwnedPeripheral<BasePeripheralOwner>(TYPE, owner) {
    override val isEnabled = true

    private val subscriptionTracker = AE2StorageSubscriptionTracker()
    private val subscriptionObserver = AE2WirelessStorageObserver(subscriptionTracker)

    init {
        val storage = owner.dataStorage
        val malformed = storage.loadAE2StorageSubscriptions(subscriptionTracker)
        subscriptionTracker.onDefinitionsChanged = { storage.putAE2StorageSubscriptions(subscriptionTracker) }
        subscriptionTracker.onActivityChanged = { owner.level?.server?.execute(::updateSubscriptions) }
        if (malformed) storage.putAE2StorageSubscriptions(subscriptionTracker)
        addPlugin(AE2StorageSubscriptionPlugin(subscriptionTracker))
        addPlugin(AE2WirelessTerminalPlugin(owner, subscriptionObserver::destroy))
        addPlugin(AE2CraftingJobsPlugin.forWirelessComputer(owner))
    }

    internal fun updateSubscriptions() {
        if (!subscriptionTracker.shouldObserve) {
            subscriptionObserver.destroy()
            return
        }
        try {
            val session = resolveWirelessSession(peripheralOwner)
            val level = peripheralOwner.level ?: throw LuaException("Linked AE2 network is unavailable")
            subscriptionObserver.update(level, peripheralOwner.pos, session.accessPointNode)
        } catch (_: LuaException) {
            subscriptionObserver.destroy()
        }
    }

    companion object {
        const val TYPE = "ae2_wireless_terminal"

        fun create(turtle: ITurtleAccess, side: TurtleSide): AE2WirelessTerminalPeripheral {
            val owner = TurtlePeripheralOwner(turtle, side).attachFuel()
            return AE2WirelessTerminalPeripheral(owner)
        }

        fun create(access: IPocketAccess): AE2WirelessTerminalPeripheral {
            val owner = PocketPeripheralOwner(access).attachFuel()
            return AE2WirelessTerminalPeripheral(owner)
        }
    }
}

private class AE2WirelessTerminalPlugin(
    private val owner: BasePeripheralOwner,
    private val onUnavailable: () -> Unit,
) : IPeripheralPlugin {
    override val additionalType = "ae2_network_access"

    private fun resolve(): AE2WirelessSession = try {
        resolveWirelessSession(owner)
    } catch (error: LuaException) {
        onUnavailable()
        throw error
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
}
