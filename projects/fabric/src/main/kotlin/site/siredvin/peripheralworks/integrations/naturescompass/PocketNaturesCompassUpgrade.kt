package site.siredvin.peripheralworks.integrations.naturescompass

import dan200.computercraft.api.pocket.IPocketAccess
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.configuration.integration.NaturesCompassConfiguration
import site.siredvin.tweakium.modules.peripheral.owner.PocketPeripheralOwner
import site.siredvin.tweakium.modules.pocket.BasePocketUpgrade

class PocketNaturesCompassUpgrade(stack: ItemStack) :
    BasePocketUpgrade<NaturesCompassPeripheral<PocketPeripheralOwner>>(
        TYPE,
        stack,
    ) {

    companion object {
        val TYPE = ResourceLocation(PeripheralWorksCore.MOD_ID, NaturesCompassPeripheral.TYPE)
    }

    override fun getPeripheral(access: IPocketAccess): NaturesCompassPeripheral<PocketPeripheralOwner> = NaturesCompassPeripheral(PocketPeripheralOwner(access), NaturesCompassConfiguration.enableNaturesCompassPocketUpgrade)
}
