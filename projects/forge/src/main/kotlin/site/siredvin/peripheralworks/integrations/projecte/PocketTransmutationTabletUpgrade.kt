package site.siredvin.peripheralworks.integrations.projecte

import dan200.computercraft.api.pocket.IPocketAccess
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.tweakium.modules.peripheral.owner.PocketPeripheralOwner
import site.siredvin.tweakium.modules.pocket.BasePocketUpgrade

class PocketTransmutationTabletUpgrade(stack: ItemStack) :
    BasePocketUpgrade<TransmutationTabletPeripheral<PocketPeripheralOwner>>(
        TYPE,
        stack,
    ) {

    companion object {
        val TYPE = ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, TransmutationTabletPeripheral.TYPE)
    }

    override fun getPeripheral(access: IPocketAccess): TransmutationTabletPeripheral<PocketPeripheralOwner> = TransmutationTabletPeripheral(PocketPeripheralOwner(access), Configuration.enableTransmutationTabletPocketUpgrade)
}
