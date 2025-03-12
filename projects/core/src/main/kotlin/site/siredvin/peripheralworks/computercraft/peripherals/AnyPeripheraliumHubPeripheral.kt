package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.upgrades.UpgradeBase
import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.core.Holder
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.broccolium.modules.platform.api.RegistryWrapper
import site.siredvin.peripheralworks.computercraft.modem.LocalWrapper
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralOwner
import site.siredvin.tweakium.modules.platform.ComputerPlatformRegistries
import site.siredvin.tweakium.modules.platform.ComputerPlatformToolkit

abstract class AnyPeripheraliumHubPeripheral<Z : IPeripheralOwner, P : LocalWrapper<U>, U : UpgradeBase>(maxUpdateCount: Int, owner: Z, type: String) :
    PeripheraliumHubPeripheral<Z>(
        maxUpdateCount,
        owner,
        type,
    ) {

    val activeWrappers: MutableList<P> = mutableListOf()
    abstract val registry: RegistryWrapper<U>

    init {
        activeUpgrades.forEach {
            val upgrade = getUpgrade(it)
            if (upgrade != null) {
                val key = registry.getResourceKey(upgrade).get()
                val holder = registry.get(key).get()
                connectUpgrade(UpgradeData.of(holder, getDataForUpgradeAsComponent(it, peripheralOwner.dataStorage)))
            }
        }
    }

    abstract fun getUpgrade(id: String): U?
    abstract fun getUpgrade(stack: ItemStack): UpgradeData<U>?
    abstract fun wrap(owner: Z, holder: Holder.Reference<U>, id: String): P

    fun connectUpgrade(upgrade: UpgradeData<U>) {
        val id = registry.getKey(upgrade.upgrade())
        val key = registry.getResourceKey(upgrade.upgrade()).get()
        val holder = registry.get(key).get()
        val wrapper = wrap(peripheralOwner, holder, id.toString())
        if (!upgrade.data.isEmpty) setDataForUpdate(wrapper.id, upgrade.data)
        activeWrappers.add(wrapper)
        if (wrapper.peripheral != null) {
            attachRemotePeripheral(wrapper.peripheral!!, id.toString())
        }
    }

    fun disconnectPocketUpgrade(upgrade: UpgradeData<U>) {
        val id = registry.getKey(upgrade.upgrade())
        val wrapper = activeWrappers.find { it.upgrade.key().location().equals(id) } ?: return
        activeWrappers.remove(wrapper)
        @Suppress("CAST_NEVER_SUCCEEDS")
        setDataForUpdate(wrapper.id, null as? CompoundTag)
        removeRemotePeripheral(id.toString())
    }

    private fun attachExactUpgrade(id: ResourceLocation, upgrade: UpgradeData<U>) {
        attachUpgrade(id)
        connectUpgrade(upgrade)
    }

    private fun detachExactUpgrade(id: ResourceLocation, upgrade: UpgradeData<U>) {
        detachUpgrade(id)
        disconnectPocketUpgrade(upgrade)
    }

    override fun isUpgradeImpl(stack: ItemStack): Boolean = getUpgrade(stack) != null

    override fun isEquitable(stack: ItemStack): Pair<Boolean?, String?> {
        val upgrade = ComputerPlatformToolkit.get().getPocketUpgrade(PlatformToolkit.get().registries!!, stack) ?: return Pair(null, "Item is not an upgrade")
        val id = ComputerPlatformRegistries.POCKET_UPGRADES.getKey(upgrade.upgrade())
        if (activeWrappers.any { it.upgrade.key().location().equals(id) }) {
            return Pair(null, "Duplicate upgrades are not allowed")
        }
        return Pair(true, null)
    }

    override fun equipImpl(stack: ItemStack): Pair<Boolean?, String?> {
        val upgrade = getUpgrade(stack) ?: return Pair(null, "Item is not an upgrade")
        val id = registry.getKey(upgrade.upgrade())
        if (activeWrappers.any { it.upgrade.key().location().equals(id) }) {
            return Pair(null, "Duplicate upgrades are not allowed")
        }
        attachExactUpgrade(id, upgrade)
        return Pair(true, null)
    }

    override fun unequipImpl(id: String): ItemStack {
        val upgrade = activeWrappers.find { it.upgrade.key().location().toString() == id } ?: return ItemStack.EMPTY
        val upgradeStack = upgrade.fullUpgradeData.upgradeItem
        detachExactUpgrade(upgrade.upgrade.key().location(), upgrade.fullUpgradeData)
        return upgradeStack
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnyPeripheraliumHubPeripheral<*, *, *>) return false
        if (!super.equals(other)) return false

        if (activeWrappers != other.activeWrappers) return false

        return true
    }

    fun swapUpgrade(old: UpgradeData<U>?, new: UpgradeData<U>?) {
        if (old != null) {
            detachExactUpgrade(registry.getKey(old.upgrade()), old)
        }
        if (new != null) {
            attachExactUpgrade(registry.getKey(new.upgrade()), new)
        }
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + activeWrappers.hashCode()
        return result
    }
}
