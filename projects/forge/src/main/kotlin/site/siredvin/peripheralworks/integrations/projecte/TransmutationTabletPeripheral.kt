package site.siredvin.peripheralworks.integrations.projecte

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import moze_intel.projecte.api.ItemInfo
import moze_intel.projecte.api.capabilities.IKnowledgeProvider
import moze_intel.projecte.api.capabilities.PECapabilities
import moze_intel.projecte.api.event.PlayerAttemptLearnEvent
import moze_intel.projecte.emc.nbt.NBTManager
import moze_intel.projecte.utils.EMCHelper
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Items
import net.minecraftforge.common.MinecraftForge
import site.siredvin.broccolium.modules.platform.PlatformRegistries
import site.siredvin.broccolium.modules.storage.item.ItemStorageUtils
import site.siredvin.tweakium.modules.peripheral.OwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation
import site.siredvin.tweakium.modules.peripheral.representation.RepresentationMode
import site.siredvin.tweakium.modules.peripheral.util.assertBetween
import site.siredvin.tweakium.modules.plugins.SuppliedRudimentInventoryPlugin
import java.math.BigInteger

class TransmutationTabletPeripheral<O : IPeripheralOwner>(peripheralOwner: O, override val isEnabled: Boolean) : OwnedPeripheral<O>(TYPE, peripheralOwner) {
    companion object {
        const val TYPE = "transmutation_table"
    }

    init {
        addPlugin(SuppliedRudimentInventoryPlugin({ peripheralOwner.level!! }, { peripheralOwner.storage!! }))
    }

    private val knowledge: IKnowledgeProvider?
        get() = peripheralOwner.owner?.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY)?.resolve()?.get()

    @LuaFunction(mainThread = true)
    fun getEMC(): BigInteger = knowledge?.emc ?: BigInteger.ZERO

    @LuaFunction(mainThread = true)
    fun getAvailableItems(): List<Map<String, Any>> = knowledge?.knowledge?.map {
        val base = LuaRepresentation.forItemStack(it.createStack(), RepresentationMode.BASE)
        base.remove("count")
        base
    } ?: emptyList()

    @LuaFunction(mainThread = true)
    fun syntize(id: String, amount: Int): MethodResult {
        val kp = knowledge ?: return MethodResult.of(null, "Cannot find player")
        val item = PlatformRegistries.ITEMS.get(ResourceLocation.parse(id))
        if (item == Items.AIR) {
            return MethodResult.of(null, "There is no such item")
        }
        val itemInfo = kp.knowledge.firstOrNull { it.item == item } ?: return MethodResult.of(null, "Such item is not learned")
        val cost = EMCHelper.getEmcValue(itemInfo)
        if (kp.emc.toLong() < cost * amount) {
            return MethodResult.of(null, "Not enough EMC")
        }
        val stack = itemInfo.createStack().copyWithCount(amount)
        ItemStorageUtils.toInventoryOrToWorld(stack, peripheralOwner.storage!!, peripheralOwner.pos, peripheralOwner.level!!)
        kp.emc -= BigInteger.valueOf(cost * amount)
        kp.syncEmc(peripheralOwner.owner!! as ServerPlayer)
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun transmute(slot: Int): MethodResult {
        val kp = knowledge ?: return MethodResult.of(null, "Cannot find player")
        val inventory = peripheralOwner.storage ?: return MethodResult.of(null, "Cannot find inventory")
        assertBetween(slot, 1, inventory.size, "slot")
        val realSlot = slot - 1
        val stack = inventory.getItem(realSlot)
        if (!EMCHelper.doesItemHaveEmc(stack)) {
            return MethodResult.of(null, "Cannot transmute item")
        }
        val cost = EMCHelper.getEmcSellValue(stack) * stack.count
        if (cost == 0L) {
            return MethodResult.of(null, "Cannot transmute item")
        }
        val realStack = inventory.takeItems(stack.count, realSlot, realSlot, ItemStorageUtils.ALWAYS)
        if (realStack.isEmpty) {
            return MethodResult.of(null, "Something gone wrong")
        }
        kp.emc += BigInteger.valueOf(EMCHelper.getEmcSellValue(realStack) * realStack.count)
        val info = ItemInfo.fromStack(realStack)
        val cleanedInfo = NBTManager.getPersistentInfo(info)
        val player = peripheralOwner.owner!! as ServerPlayer
        if (!kp.hasKnowledge(cleanedInfo) &&
            !MinecraftForge.EVENT_BUS.post(
                PlayerAttemptLearnEvent(
                    player,
                    info,
                    cleanedInfo,
                ),
            ) &&
            kp.addKnowledge(cleanedInfo)
        ) {
            kp.syncKnowledgeChange(player, cleanedInfo, true)
        }
        kp.syncEmc(player)
        return MethodResult.of(true)
    }
}
