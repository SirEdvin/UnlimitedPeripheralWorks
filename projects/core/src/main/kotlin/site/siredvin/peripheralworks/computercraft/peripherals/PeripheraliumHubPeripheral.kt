package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.StringTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralium.api.peripheral.IPeripheralOwner
import site.siredvin.peripheralium.storages.item.ItemStorageUtils
import site.siredvin.peripheralium.util.assertBetween
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.modem.ModemPeripheral

abstract class PeripheraliumHubPeripheral<O : IPeripheralOwner>(private val maxUpdateCount: Int, owner: O, type: String) : ModemPeripheral<O>(type, owner) {

    companion object {
        const val TYPE = "peripheralium_hub"
        const val NETHERITE_TYPE = "netherite_$TYPE"
        const val UPGRADES_TAG = "connectedUpgrades"
        const val MODE_TAG = "mode"
        val ID = ResourceLocation(PeripheralWorksCore.MOD_ID, TYPE)
        val NETHERITE_ID = ResourceLocation(PeripheralWorksCore.MOD_ID, NETHERITE_TYPE)
        const val TWEAKED_STORAGES = "__TWEAKED_STORAGES__"
    }

    /**
     * So, we don't expect this peripheral to really use plugins for now
     * and with this in mind, any additional type will be rejected
     */
    override fun getAdditionalTypes(): Set<String> {
        return setOf("peripheral_hub")
    }
    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enablePeripheraliumHubs

    protected val activeUpgrades: List<String>
        get() = peripheralOwner.dataStorage.getList(UPGRADES_TAG, 8).map { it.asString }

    abstract val activeMode: String

    abstract fun isUpgradeImpl(stack: ItemStack): Boolean

    abstract fun isEquitable(stack: ItemStack): Pair<Boolean?, String?>

    abstract fun equipImpl(stack: ItemStack): Pair<Boolean?, String?>

    abstract fun unequipImpl(id: String): ItemStack

    protected fun attachUpgrade(id: ResourceLocation) {
        val upgradeList = peripheralOwner.dataStorage.getList(UPGRADES_TAG, 8)
        upgradeList.add(StringTag.valueOf(id.toString()))
        peripheralOwner.dataStorage.put(UPGRADES_TAG, upgradeList)
        if (upgradeList.isNotEmpty()) peripheralOwner.dataStorage.putString(MODE_TAG, activeMode)
    }

    protected fun detachUpgrade(id: ResourceLocation) {
        val upgradeList = peripheralOwner.dataStorage.getList(UPGRADES_TAG, 8)
        upgradeList.remove(StringTag.valueOf(id.toString()))
        peripheralOwner.dataStorage.put(UPGRADES_TAG, upgradeList)
        if (upgradeList.isEmpty()) peripheralOwner.dataStorage.remove(MODE_TAG)
    }

    fun getDataForUpgrade(id: String): CompoundTag {
        val base = peripheralOwner.dataStorage
        if (!base.contains(TWEAKED_STORAGES)) {
            base.put(TWEAKED_STORAGES, CompoundTag())
        }
        val tweakedStorages = base.getCompound(TWEAKED_STORAGES)
        if (!tweakedStorages.contains(id)) {
            tweakedStorages.put(id, CompoundTag())
        }
        return tweakedStorages.getCompound(id)
    }

    fun setDataForUpdate(id: String, data: CompoundTag?) {
        val base = peripheralOwner.dataStorage
        if (!base.contains(TWEAKED_STORAGES))
            base.put(TWEAKED_STORAGES, CompoundTag())
        val tweakedStorages = base.getCompound(TWEAKED_STORAGES)
        if (data == null) {
            tweakedStorages.remove(id)
        } else {
            tweakedStorages.put(id, data)
        }
    }

    @LuaFunction(mainThread = true)
    fun isUpgrade(slot: Int): Boolean {
        val storage = peripheralOwner.storage ?: return false
        assertBetween(slot, 1, storage.size, "Slot should be between 1 and ${storage.size}")
        val stack = storage.getItem(slot - 1)
        return isUpgradeImpl(stack)
    }

    @LuaFunction(mainThread = true)
    fun equip(slot: Int): MethodResult {
        if (activeUpgrades.size > maxUpdateCount) {
            throw LuaException("Cannot add new upgrade, maximum upgrade count for this hub is $maxUpdateCount")
        }
        val storage = peripheralOwner.storage ?: return MethodResult.of(null, "Cannot access inventory for some reason")
        assertBetween(slot, 1, storage.size, "Slot should be between 1 and ${storage.size}")
        val stack = storage.getItem(slot - 1)
        val equipTestResult = isEquitable(stack)
        if (equipTestResult.first == null || !equipTestResult.first!!) {
            return MethodResult.of(equipTestResult.first, equipTestResult.second)
        }
        val takenStack: ItemStack = storage.takeItems(1, slot - 1, slot - 1, ItemStorageUtils.ALWAYS)
        if (takenStack.isEmpty) {
            return MethodResult.of(null, "Cannot extract item for equipment")
        }
        val equipResult = equipImpl(takenStack)
        if (equipResult.first == null || !equipResult.first!!) {
            storage.storeItem(takenStack, slot - 1, slot - 1)
            return MethodResult.of(equipResult.first, equipResult.second)
        }
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun unequip(id: String): MethodResult {
        val storage = peripheralOwner.storage ?: return MethodResult.of(null, "Cannot access inventory for some reason")
        if (!activeUpgrades.contains(id)) {
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
    fun getUpgrades(): List<String> {
        return activeUpgrades
    }
}
