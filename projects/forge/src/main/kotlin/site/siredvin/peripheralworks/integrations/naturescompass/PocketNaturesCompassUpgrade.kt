package site.siredvin.peripheralworks.integrations.naturescompass

import dan200.computercraft.api.pocket.IPocketAccess
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralium.computercraft.peripheral.owner.PocketPeripheralOwner
import site.siredvin.peripheralium.computercraft.pocket.BasePocketUpgrade
import site.siredvin.peripheralworks.PeripheralWorksCore

class PocketNaturesCompassUpgrade(stack: ItemStack) : BasePocketUpgrade<NaturesCompassPeripheral<PocketPeripheralOwner>>(
    TYPE,
    stack,
) {

    companion object {
        val TYPE = ResourceLocation(PeripheralWorksCore.MOD_ID, NaturesCompassPeripheral.TYPE)
    }

    override fun getPeripheral(access: IPocketAccess): NaturesCompassPeripheral<PocketPeripheralOwner> {
        return NaturesCompassPeripheral(PocketPeripheralOwner(access), Configuration.enableNaturesCompassPocketUpgrade)
    }
}
