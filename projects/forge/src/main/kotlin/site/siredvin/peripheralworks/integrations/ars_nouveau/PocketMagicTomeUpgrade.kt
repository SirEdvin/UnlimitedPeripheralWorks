package site.siredvin.peripheralworks.integrations.ars_nouveau

import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry
import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.upgrades.UpgradeType
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import site.siredvin.peripheralworks.common.configuration.integration.ArsNouveauConfiguration
import site.siredvin.tweakium.modules.peripheral.owner.PocketPeripheralOwner
import site.siredvin.tweakium.modules.pocket.BasePocketUpgrade

class PocketMagicTomeUpgrade(type: ResourceLocation, stack: ItemStack) :
    BasePocketUpgrade<MagicTomePeripheral>(
        type,
        stack,
    ) {

    override fun getType(): UpgradeType<out IPocketUpgrade> = Integration.magicTomeUpgradeType.get()

    override fun getPeripheral(access: IPocketAccess): MagicTomePeripheral = MagicTomePeripheral(PocketPeripheralOwner(access), getUpgradeItem(access.upgradeData), ArsNouveauConfiguration.enableCasterTomePocketUpgrade)

    override fun getUpgradeData(stack: ItemStack): DataComponentPatch = DataComponentPatch.builder().apply {
        stack.get(DataComponentRegistry.SPELL_CASTER.get())?.let { set(DataComponentRegistry.SPELL_CASTER.get(), it) }
        stack.get(DataComponentRegistry.TOME_CASTER.get())?.let { set(DataComponentRegistry.TOME_CASTER.get(), it) }
    }.build()

    override fun getUpgradeItem(upgradeData: DataComponentPatch): ItemStack {
        if (upgradeData.isEmpty) return craftingItem
        val base = craftingItem.copy()
        base.applyComponents(upgradeData)
        return base
    }

    override fun onRightClick(world: Level?, access: IPocketAccess?, peripheral: IPeripheral?): Boolean = super.onRightClick(world, access, peripheral)

    override fun isItemSuitable(stack: ItemStack): Boolean {
        val tweakedStack = stack.copy()
        tweakedStack.remove(DataComponentRegistry.SPELL_CASTER.get())
        tweakedStack.remove(DataComponentRegistry.TOME_CASTER.get())
        return super.isItemSuitable(tweakedStack)
    }
}
