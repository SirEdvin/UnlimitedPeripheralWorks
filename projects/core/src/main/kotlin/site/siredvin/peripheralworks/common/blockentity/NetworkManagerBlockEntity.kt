package site.siredvin.peripheralworks.common.blockentity

import dan200.computercraft.api.network.wired.WiredElement
import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.shared.computer.core.ServerContext
import dan200.computercraft.shared.peripheral.modem.wired.WiredModemElement
import dan200.computercraft.shared.platform.ComponentAccess
import dan200.computercraft.shared.platform.PlatformHelper
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.StringTag
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import site.siredvin.broccolium.modules.base.api.IObservingBlockEntity
import site.siredvin.broccolium.modules.base.block.FacingBlockEntityBlock
import site.siredvin.broccolium.modules.base.ext.toVec3
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.block.NetworkManager
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.computercraft.peripherals.NetworkManagerPeripheral
import site.siredvin.tweakium.modules.peripheral.blockentity.MutablePeripheralBlockEntity
import java.util.function.Consumer

class NetworkManagerBlockEntity(blockPos: BlockPos, blockState: BlockState) :
    MutablePeripheralBlockEntity<NetworkManagerPeripheral>(BlockEntityTypes.NETWORK_MANAGER.get(), blockPos, blockState),
    IObservingBlockEntity {

    companion object {
        const val DISPLAY_PERIPHERALS_TAG = "displayPeripherals"
        const val PERIPHERAL_GROUPS = "peripheralGroups"
        const val PERIPHERAL_NAME = "peripheralName"
    }

    class PeripheralGroup {
        val peripherals: MutableSet<String> = mutableSetOf()
        var color: Int = -1

        fun toNBT(): CompoundTag {
            val data = CompoundTag()
            data.putInt("color", color)
            val list = ListTag()
            peripherals.forEach {
                list.add(StringTag.valueOf(it))
            }
            data.put("peripherals", list)
            return data
        }

        override fun toString(): String = "PeripheralGroup(peripherals=$peripherals, color=$color)"

        companion object {
            fun fromNBT(tag: CompoundTag): PeripheralGroup {
                val group = PeripheralGroup()
                group.color = tag.getInt("color")
                val list = tag.getList("peripherals", StringTag.TAG_STRING.toInt())
                list.forEach {
                    group.peripherals.add(it.asString)
                }
                return group
            }
        }
    }

    class NetworkManagerWiredElement(private val be: NetworkManagerBlockEntity) : WiredModemElement() {
        override fun attachPeripheral(
            name: String,
            peripheral: IPeripheral,
        ) {
            be.attachPeripheral(name, peripheral)
        }

        override fun detachPeripheral(name: String) {
            be.detachPeripheral(name)
        }

        override fun getSenderID(): String = NetworkManagerPeripheral.TYPE

        override fun getLevel(): Level? = be.level

        override fun getPosition(): Vec3 = be.blockPos.toVec3()
    }

    class DrawingInstructions(val peripheralName: String) {
        val extraNames = mutableListOf<String>()
        val groups = mutableListOf<String>()
    }

    val peripherals: MutableMap<String, BlockPos> = mutableMapOf()
    val displayPeripherals: MutableMap<String, BlockPos> = mutableMapOf()
    val peripheralGroups: MutableMap<String, PeripheralGroup> = mutableMapOf()
    val clientBlockCache = mutableMapOf<BlockPos, DrawingInstructions>()
    val element = NetworkManagerWiredElement(this)
    private var peripheralName: String? = null
    private var refreshConnectionsRequired: Boolean = true
    private var begsForTick: Boolean = true
    private var peripheralRegistered: Boolean = false
    private val connectedElements: ComponentAccess<WiredElement> =
        PlatformHelper.get().createWiredElementAccess(
            this,
            Consumer { x: Direction ->
                scheduleConnectionsChanged()
            },
        )

    override fun createPeripheral(side: Direction): NetworkManagerPeripheral = NetworkManagerPeripheral(this)

    fun attachPeripheral(name: String, peripheral: IPeripheral) {
        val pos = extractPeripheralPos(peripheral.target)
        if (pos != null) {
            peripherals[name] = pos
            pushData()
        } else {
            PeripheralWorksCore.logger.warn("Cannot locate anything about {} skipping it", name)
        }
        satisfyBegForTicks()
    }

    fun detachPeripheral(name: String) {
        peripherals.remove(name)
        pushData()
        satisfyBegForTicks()
    }

    fun toggleGroup(name: String, peripheralName: String) {
        if (!peripherals.contains(peripheralName)) return
        if (!peripheralGroups.contains(name)) {
            val newGroup = PeripheralGroup()
            newGroup.peripherals.add(peripheralName)
            peripheralGroups[name] = newGroup
            peripheral?.queueEvent("network_manager_group_change", newGroup, "added", peripheralName)
        } else {
            val group = peripheralGroups[name] ?: return
            if (group.peripherals.contains(peripheralName)) {
                group.peripherals.remove(peripheralName)
                peripheral?.queueEvent("network_manager_group_change", name, "removed", peripheralName)
            } else {
                group.peripherals.add(peripheralName)
                peripheral?.queueEvent("network_manager_group_change", name, "added", peripheralName)
            }
        }
        pushData()
    }

    fun pushData() {
        if (!isRemoved) {
            pushInternalDataChangeToClient(blockState.setValue(NetworkManager.TOGGLING, !blockState.getValue(NetworkManager.TOGGLING)))
        }
    }

    private fun extractPeripheralPos(something: Any?): BlockPos? {
        if (something == null) {
            return null
        }
        if (something is BlockPos) {
            return something
        }
        if (something is BlockEntity) {
            return something.blockPos
        }
        return null
    }

    private fun scheduleConnectionsChanged() {
        refreshConnectionsRequired = true
        if (level != null && !isRemoved) {
            level!!.scheduleTick(blockPos, blockState.block, 0)
        } else {
            begsForTick = true
        }
    }

    private fun refreshConnection() {
        val level = getLevel() ?: return
        if (level.isClientSide || isRemoved) return
        refreshConnectionsRequired = false
        var isConnected = false

        val current = blockPos

        for (direction in Direction.entries) {
            val offset = current.relative(direction)
            if (!level.isLoaded(offset)) continue
            if (direction == blockState.getValue(FacingBlockEntityBlock.FACING)) continue

            val element = connectedElements.get(direction) ?: continue
            this.element.node.connectTo(element.node)
            isConnected = true
        }
        val wasConnected = blockState.getValue(NetworkManager.CONNECTED)
        if (wasConnected != isConnected) {
            pushInternalDataChangeToClient(blockState.setValue(NetworkManager.CONNECTED, isConnected))
        } else {
            pushData()
        }
    }

    override fun saveInternalData(data: CompoundTag): CompoundTag {
        val displayPeripheralsTag = CompoundTag()
        peripherals.entries.forEach {
            displayPeripheralsTag.put(it.key, NbtUtils.writeBlockPos(it.value))
        }
        data.put(DISPLAY_PERIPHERALS_TAG, displayPeripheralsTag)
        if (peripheralName != null) {
            data.putString(PERIPHERAL_NAME, peripheralName!!)
        }
        if (peripheralGroups.isNotEmpty()) {
            val peripheralGroupTag = CompoundTag()
            peripheralGroups.entries.forEach {
                peripheralGroupTag.put(it.key, it.value.toNBT())
            }
            data.put(PERIPHERAL_GROUPS, peripheralGroupTag)
        }
        return data
    }

    fun satisfyBegForTicks() {
        if (hasLevel() && !level!!.isClientSide && begsForTick && !isRemoved) {
            PeripheralWorksCore.logger.warn("Satisfy begs for ticks")
            level!!.scheduleTick(blockPos, blockState.block, 0)
            begsForTick = false
        }
    }

    override fun loadInternalData(
        data: CompoundTag,
        state: BlockState?,
    ): BlockState {
        if (data.contains(DISPLAY_PERIPHERALS_TAG)) {
            val displayPeripheralsTag = data.getCompound(DISPLAY_PERIPHERALS_TAG)
            displayPeripherals.clear()
            displayPeripheralsTag.allKeys.forEach {
                displayPeripherals[it] = NbtUtils.readBlockPos(displayPeripheralsTag.getCompound(it))
            }
        }
        if (data.contains(PERIPHERAL_NAME)) {
            peripheralName = data.getString(PERIPHERAL_NAME)
        }
        if (data.contains(PERIPHERAL_GROUPS)) {
            peripheralGroups.clear()
            val peripheralGroupsTag = data.getCompound(PERIPHERAL_GROUPS)
            peripheralGroupsTag.allKeys.forEach {
                peripheralGroups[it] = PeripheralGroup.fromNBT(peripheralGroupsTag.getCompound(it))
            }
        }
        if (hasLevel() && level!!.isClientSide) {
            clientBlockCache.clear()
            displayPeripherals.entries.forEach {
                if (!clientBlockCache.contains(it.value)) {
                    clientBlockCache[it.value] = DrawingInstructions(it.key)
                } else {
                    clientBlockCache[it.value]!!.extraNames.add(it.key)
                }
                peripheralGroups.entries.forEach { group ->
                    if (group.value.peripherals.contains(it.key)) {
                        clientBlockCache[it.value]!!.groups.add(group.key)
                    }
                }
            }
        }
        return state ?: blockState
    }

    private fun buildNewPeripheralName() {
        val newPeripheralID = ServerContext.get(PlatformToolkit.get().minecraftServer!!).getNextId("peripheral.${NetworkManagerPeripheral.TYPE}")
        peripheralName = "${NetworkManagerPeripheral.TYPE}_$newPeripheralID"
    }

    override fun blockTick() {
        if (hasLevel() && !level!!.isClientSide && !isRemoved) {
            begsForTick = false
            if (refreshConnectionsRequired) {
                refreshConnection()
            }
            if (!peripheralRegistered) {
                ensurePeripheralCreated(Direction.UP)
                if (peripheralName == null) {
                    buildNewPeripheralName()
                }
                this.element.node.updatePeripherals(
                    mapOf(
                        peripheralName!! to peripheral!!,
                    ),
                )
                peripheralRegistered = true
            }
        }
    }

    override fun onNeighbourChange(neighbour: BlockPos) {
        scheduleConnectionsChanged()
    }

    override fun setRemoved() {
        super.setRemoved()
        if (level == null || !level!!.isClientSide) {
            this.element.node.remove()
        }
    }

    override fun placed() {
        scheduleConnectionsChanged()
    }

    override fun destroy() {
        if (level == null || !level!!.isClientSide) {
            this.element.node.remove()
        }
    }
}
