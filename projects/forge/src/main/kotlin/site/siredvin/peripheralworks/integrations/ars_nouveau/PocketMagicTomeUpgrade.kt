package site.siredvin.peripheralworks.integrations.ars_nouveau

import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.pocket.IPocketAccess
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import site.siredvin.tweakium.modules.peripheral.owner.PocketPeripheralOwner
import site.siredvin.tweakium.modules.pocket.BasePocketUpgrade

class PocketMagicTomeUpgrade(type: ResourceLocation, stack: ItemStack) :
    BasePocketUpgrade<MagicTomePeripheral>(
        type,
        stack,
    ) {

    companion object {
        val STORED_DATA_TAG = ResourceLocation("ars_nouveau", "caster").toString()
    }

    override fun getPeripheral(access: IPocketAccess): MagicTomePeripheral = MagicTomePeripheral(PocketPeripheralOwner(access), access.upgrade!!.upgradeItem, Configuration.enableCasterTomePocketUpgrade)

    override fun getUpgradeData(stack: ItemStack): CompoundTag {
        return stack.getTagElement(STORED_DATA_TAG) ?: return CompoundTag()
    }

    override fun getUpgradeItem(upgradeData: CompoundTag): ItemStack {
        if (upgradeData.isEmpty) return craftingItem
        val base = craftingItem.copy()
        base.addTagElement(STORED_DATA_TAG, upgradeData)
        return base
    }

    override fun onRightClick(world: Level?, access: IPocketAccess?, peripheral: IPeripheral?): Boolean = super.onRightClick(world, access, peripheral)

    override fun isItemSuitable(stack: ItemStack): Boolean {
        if (stack.getTagElement(STORED_DATA_TAG) == null) return super.isItemSuitable(stack)
        val tweakedStack = stack.copy()
        tweakedStack.orCreateTag.remove(STORED_DATA_TAG)
        return super.isItemSuitable(tweakedStack)
    }
}
