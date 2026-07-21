package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.upgrades.UpgradeBase
import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponentType
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.broccolium.modules.storage.item.ItemStorageUtils
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.components.PeripheralUpgrades
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.modem.LocalWrapper
import site.siredvin.peripheralworks.computercraft.modem.PeripheralHubPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.util.assertBetween
import site.siredvin.tweakium.modules.platform.ComputerPlatformToolkit
import kotlin.jvm.optionals.getOrDefault

abstract class PeripheraliumHubPeripheral<O : IPeripheralOwner, T : UpgradeBase, W : LocalWrapper<T>>(private val maxUpdateCount: Int, owner: O, type: String) : PeripheralHubPeripheral<O>(type, owner) {

    companion object {
        const val TYPE = "peripheralium_hub"
        const val NETHERITE_TYPE = "netherite_$TYPE"
        val ID = ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, TYPE)
        val NETHERITE_ID = ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, NETHERITE_TYPE)
    }

    val activeWrappers: MutableList<W> = mutableListOf()

    init {
        activeUpgrades.forEach {
            connectUpgrade(it)
        }
    }

    /**
     * So, we don't expect this peripheral to really use plugins for now
     * and with this in mind, any additional type will be rejected
     */
    override fun getAdditionalTypes(): Set<String> = setOf("peripheral_hub")
    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enablePeripheraliumHubs

    protected val activeUpgrades: List<UpgradeData<T>>
        get() = peripheralOwner.dataStorage.patch.get(component)?.get()?.upgrades ?: listOf()

    abstract val component: DataComponentType<PeripheralUpgrades<T>>
    abstract fun getUpgrade(stack: ItemStack): UpgradeData<T>?
    abstract fun wrap(owner: O, data: UpgradeData<T>): W

    fun isUpgradeImpl(stack: ItemStack): Boolean = getUpgrade(stack) != null

    fun isEquitable(stack: ItemStack): Pair<Boolean?, String?> {
        val upgrade = ComputerPlatformToolkit.get().getPocketUpgrade(PlatformToolkit.get().registries!!, stack) ?: return Pair(null, "Item is not an upgrade")
        if (activeUpgrades.any { it.holder.key() == upgrade.holder.key() }) {
            return Pair(null, "Duplicate upgrades are not allowed")
        }
        return Pair(true, null)
    }

    fun equipImpl(stack: ItemStack): Pair<Boolean?, String?> {
        val upgrade = getUpgrade(stack) ?: return Pair(null, "Item is not an upgrade")
        if (activeUpgrades.any { it.holder.key() == upgrade.holder.key() }) {
            return Pair(null, "Duplicate upgrades are not allowed")
        }
        attachUpgrade(upgrade)
        connectUpgrade(upgrade)
        return Pair(true, null)
    }

    fun unequipImpl(id: String): ItemStack {
        val wrapper = activeWrappers.find { it.fullUpgrade.holder.key().location().toString() == id } ?: return ItemStack.EMPTY
        detachUpgrade(wrapper.fullUpgrade)
        disconnectUpgrade(wrapper.fullUpgrade)
        return wrapper.fullUpgrade.upgradeItem
    }

    protected fun attachUpgrade(upgrade: UpgradeData<T>) {
        var peripheralUpgrades = peripheralOwner.dataStorage.patch.get(component)?.getOrDefault(PeripheralUpgrades()) ?: PeripheralUpgrades()
        peripheralUpgrades = PeripheralUpgrades(peripheralUpgrades.upgrades + upgrade)
        peripheralOwner.dataStorage.patch = DataComponentPatch.builder().set(component, peripheralUpgrades).build()
    }

    protected fun detachUpgrade(upgrade: UpgradeData<T>) {
        var peripheralUpgrades = peripheralOwner.dataStorage.patch.get(component)?.getOrDefault(PeripheralUpgrades()) ?: PeripheralUpgrades()
        peripheralUpgrades = PeripheralUpgrades(peripheralUpgrades.upgrades.filter { it.holder.key().equals(upgrade.holder.key()) })
        peripheralOwner.dataStorage.patch = DataComponentPatch.builder().set(component, peripheralUpgrades).build()
    }

    fun connectUpgrade(upgrade: UpgradeData<T>) {
        val wrapper = wrap(peripheralOwner, upgrade)
        if (!upgrade.data.isEmpty) setDataForUpdate(wrapper.id, upgrade.data)
        activeWrappers.add(wrapper)
        if (wrapper.peripheral != null) {
            attachRemotePeripheral(wrapper.peripheral!!, upgrade.holder.key().location().toString())
        }
    }

    fun disconnectUpgrade(upgrade: UpgradeData<T>) {
        val wrapper = activeWrappers.find { it.fullUpgrade.holder.key().equals(upgrade.holder.key()) } ?: return
        activeWrappers.remove(wrapper)
        removeRemotePeripheral(upgrade.holder.key().location().toString())
    }

    fun setDataForUpdate(id: String, data: DataComponentPatch?) {
        var peripheralUpgrades = peripheralOwner.dataStorage.patch.get(component)?.getOrDefault(PeripheralUpgrades()) ?: PeripheralUpgrades()
        val upgradeData = peripheralUpgrades.upgrades.find { it.holder.key().location().toString() == id } ?: return
        val emptyList = peripheralUpgrades.upgrades.filterNot { it == upgradeData }
        peripheralUpgrades = PeripheralUpgrades(emptyList + UpgradeData.of(upgradeData.holder, data))
        peripheralOwner.dataStorage.patch = DataComponentPatch.builder().set(component, peripheralUpgrades).build()
    }

    @LuaFunction(mainThread = true)
    fun isUpgrade(slot: Int): Boolean {
        val storage = peripheralOwner.storage ?: return false
        assertBetween(slot, 1, storage.size, "Slot should be between 1 and ${storage.size}")
        val stack = storage.get(slot - 1)
        return isUpgradeImpl(stack)
    }

    @LuaFunction(mainThread = true)
    fun equip(slot: Int): MethodResult {
        if (activeUpgrades.size > maxUpdateCount) {
            throw LuaException("Cannot add new upgrade, maximum upgrade count for this hub is $maxUpdateCount")
        }
        val storage = peripheralOwner.storage ?: return MethodResult.of(null, "Cannot access inventory for some reason")
        assertBetween(slot, 1, storage.size, "Slot should be between 1 and ${storage.size}")
        val stack = storage.get(slot - 1)
        val equipTestResult = isEquitable(stack)
        if (equipTestResult.first == null || !equipTestResult.first!!) {
            return MethodResult.of(equipTestResult.first, equipTestResult.second)
        }
        val takenStack: ItemStack = storage.take(1, slot - 1, slot - 1, ItemStorageUtils.ALWAYS, false)
        if (takenStack.isEmpty) {
            return MethodResult.of(null, "Cannot extract item for equipment")
        }
        val equipResult = equipImpl(takenStack)
        if (equipResult.first == null || !equipResult.first!!) {
            storage.store(takenStack, slot - 1, slot - 1, false)
            return MethodResult.of(equipResult.first, equipResult.second)
        }
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun unequip(id: String): MethodResult {
        val storage = peripheralOwner.storage ?: return MethodResult.of(null, "Cannot access inventory for some reason")
        if (!activeUpgrades.any { it.holder.key().location().toString() == id }) {
            throw LuaException("Cannot find upgrade with id $id")
        }
        val stack = unequipImpl(id)
        if (stack.isEmpty) {
            return MethodResult.of(null, "Cannot generate stack for unknown reason")
        }
        ItemStorageUtils.toInventoryOrToWorld(stack, storage, 0, peripheralOwner.pos, peripheralOwner.level!!)
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun getUpgrades(): List<String> = activeUpgrades.map { it.holder.key().location().toString() }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PeripheraliumHubPeripheral<*, *, *>) return false
        if (!super.equals(other)) return false

        if (activeWrappers != other.activeWrappers) return false

        return true
    }

    fun swapUpgrade(old: UpgradeData<T>?, new: UpgradeData<T>?) {
        if (old != null) {
            disconnectUpgrade(old)
            detachUpgrade(old)
        }
        if (new != null) {
            attachUpgrade(new)
            connectUpgrade(new)
        }
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + activeWrappers.hashCode()
        return result
    }
}
